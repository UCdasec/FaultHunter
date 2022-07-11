package com.afivd.afivd;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LoopCheck is a replacement pattern that adds an if-statement after a for-loop to ensure that it completed successfully.
 * Additionally, it will automatically rename the iterator variable if another variable with the same name is in the same
 * or higher scope.
 *      o May not work with for-loops that contain breaks or continues.
 * Covers Fault.LOOPCHECK
 */
public class LoopCheck extends CBaseListener implements FaultPattern{
    // Private Variables
    private final ParsedResults output;
    private final ArrayList<VariableSearcher.VariableTuple> variables;
    private final ArrayList<String> codeLines;
    private final ArrayList<String> forConditionals = new ArrayList<>();
    private final ArrayList<String> ifConditionals  = new ArrayList<>();

    // Constructor
    public LoopCheck(ParsedResults output,ArrayList<VariableSearcher.VariableTuple> variables,ArrayList<String> codeLines){
        this.variables = variables;
        this.output = output;
        this.codeLines = codeLines;
    }

    // ------------------------------------------ Listener Overrides ---------------------------------------------------
    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        if(ctx.getStart().getText().equalsIgnoreCase("for")){
            // If there is already a variable in a same or higher depth, we are forced to rename
            // If there is already a variable in a lesser depth, look inside if-statements and make sure that a loop check hasn't already been added.

            // Case 1: There is an explicit declaration and initialization of a variable in the for-loop
            if(ctx.forCondition().forDeclaration() != null){
                CParser.ForDeclarationContext forDeclaration = ctx.forCondition().forDeclaration();
                // Need to create the following structure
                //  * 'forLoopPrefix'
                //  * for( 'modifiedForDeclaration'; 'conditionalForExpression'; 'mathForExpression') \n
                //  * 'unchanged statement'
                //  * 'forLoopSuffix'

                // TODO: In the future, handle the case if there is more than one declarator in the initDeclaratorList
                // Make a count of how many variables are named the same as the for-variable
                if(forDeclaration.initDeclaratorList().initDeclarator().size() == 1){
                    String varName = forDeclaration.initDeclaratorList().initDeclarator().get(0).declarator().directDeclarator().getText();
                    // TODO: Most likely we only need to look for primitives (stopping at typeSpecifier, but could also look at typeDefName)
                    String varType = forDeclaration.declarationSpecifiers().declarationSpecifier().get(0).typeSpecifier().getText();
                    int sameNameVarCount=0;
                    for (int i = 0; i < variables.size(); i++) {
                        // If there are more than one at the same or higher depth, we will need to rename when we restructure
                        if(varName.equals(variables.get(i).getVariableName()) && variables.get(i).getDepth() >= forDeclaration.depth()){
                            sameNameVarCount++;
                        }
                    }
                    if(sameNameVarCount>1){
                        // Rename varName using an iterative method in case the iterated variable name is already taken
                        varName = iterativeRename(varName);
                    }
                    String forLoopPrefix = varType+" "+varName+";";
                    String modifiedForDeclaration = varName + " = " + forDeclaration.initDeclaratorList().initDeclarator().get(0).initializer().getText();
                    String conditionalForExpression = ctx.forCondition().forExpression(0).getText();
                    String mathForExpression = ctx.forCondition().forExpression().get(1).getText();
                    String forLoopSuffix = "if("+conditionalForExpression+"){faultDetect();}";

                    // Need to clear out the previous for-loop, so remove the characters between and including the curly braces
                    int startLine = ctx.start.getLine();
                    int endLine = ctx.stop.getLine();
                    int startChar = ctx.start.getCharPositionInLine();
                    int endChar = ctx.stop.getCharPositionInLine();

                    // Try to match indentation by checking to see if the initial substring is all whitespace, if
                    // it is, then we can copy it and use it for all our insertions. If there is actual code in the substring
                    // we will have to respect it.
                    // TODO: Find a better way to implement this indentation code later
                    String indentation = "";
                    String indentationCheck = codeLines.get(startLine-1).substring(0,startChar);
                    Pattern pattern = Pattern.compile("([\\s ]+)");
                    Matcher matcher = pattern.matcher(indentationCheck);
                    if(matcher.find()){
                        if(matcher.group(1)!= null && !matcher.group(1).equals("")){
                            indentation = matcher.group(1);
                            int spaceCount = (int)indentation.chars().filter(ch -> ch == ' ').count();
                            // TODO: Even though the regex grabs a tab, it becomes four spaces instead?
                            //  Have to manually count spaces so it looks right
                            if(spaceCount % 4 == 0){
                                for (int i = 0; i < spaceCount/4; i++) {
                                    indentation = indentation + "\t";
                                }
                            }
                        }
                    }

                    // TODO: entire for-statement body is currently on one line, maybe add a 'pretty print' system later.
                    String finishedInsertion =
                            indentation+forLoopPrefix +"\n"+
                                    indentation+ "for("+modifiedForDeclaration+"; " +conditionalForExpression+"; "+mathForExpression+")"+"\n"+
                                    indentation+"\t"+ctx.statement().getText()+"\n"+
                                    indentation+forLoopSuffix;

                    // If the for-loop spans more than two line, make sure to keep anything before the for-loop on the startLine
                    // or after the for-loop on the endLine.
                    // This is for the case when is code right before for-loop or after for-loop curly brace on same line
                    int linesToClean = endLine-startLine;
                    if(linesToClean>1){

                        // codeLines array is zero-indexed, so subtract one from startLine and endLine when used.
                        codeLines.set((startLine-1), indentation+indentationCheck);
                        codeLines.set((endLine-1), indentation+codeLines.get(endLine-1).substring(endChar+1));
                        int middleIndexStart = startLine+1;
                        int middleIndexEnd = endLine-1;
                        // Just need to blank out a single line, then put finishedInsertion
                        if(middleIndexEnd==middleIndexStart){
                            codeLines.set((middleIndexStart-1),finishedInsertion);

                        // Need to blank out multiple lines, then put finishedInsertion
                        }else if(middleIndexEnd-middleIndexStart>=1){
                            for(int i = (middleIndexStart-1); i<=(middleIndexEnd-1); i++){
                                codeLines.set(i,"");
                            }
                            codeLines.set((middleIndexStart-1),finishedInsertion);
                        }
                    // Else if we have a two line for-loop
                    }else if(linesToClean==1){
                        codeLines.set((startLine-1), codeLines.get(startLine-1).substring(0,startChar+1)+"\n"+finishedInsertion);
                        codeLines.set((endLine-1),"\n"+codeLines.get(endLine-1).substring(endChar));
                    // Else if we have a single line for-loop (for some reason)
                    }else if(linesToClean==0){
                        codeLines.set((startLine-1), codeLines.get(startLine-1).substring(0,startChar+1)
                                +finishedInsertion+codeLines.get(endLine-1).substring(endChar));
                    }

                    // TODO: Reform codeLines for a clean file
                    //for(String codeLine : codeLines){
                    //    codeLine.split("[\n]");
                    //}

                    this.output.appendResult(new ResultLine(ResultLine.SPANNING_RESULT,"loop_check","Recommended addition of loop-completion check regarding for-loop at "+startLine+" to "+endLine+". See replacements! ",startLine,endLine));

                    // TODO: If faultDetect() is not a function in the code file yet, add it
                }

            // Case 2: The for-variable has been declared elsewhere, and is being assigned to something here in the forDeclaration slot
            }else if(ctx.forCondition().forDeclaration() == null && ctx.forCondition().expression().assignmentExpression() != null){
                // Need to collect for-conditional expressions and if-conditional expressions until the end
                // If there isn't an if-conditional expression, put one after the for-loop of the for-conditional expression


            }
        }
    }

    // -------------------------------------------- Helper Functions ---------------------------------------------------

    /**
     * Given a string of an existing variable name, see if it is already being used in a lower scope to rename if necessary
     * @param varName Potential Variable Name
     * @return A String of a working variable name
     */
    public String iterativeRename(String varName){
        Pattern pattern = Pattern.compile("([a-zA-Z_]+)(\\d*)");
        Matcher matcher = pattern.matcher(varName);
        if(matcher.find()){
            String varBaseName = matcher.group(1);
            if(matcher.group(2) != null && !matcher.group(2).equals("")){
                try{
                    int newVarIteration = (Integer.parseInt(matcher.group(2)))+1;
                    String newName = varBaseName+newVarIteration;
                    for (int i = 0; i < variables.size(); i++) {
                        if(variables.get(i).getVariableName().equals(newName)){
                            i=-1;
                            newVarIteration++;
                            newName = varBaseName+newVarIteration;
                        }
                    }
                return newName;
                }catch(NumberFormatException ignored){}
            }
        }else{
            System.out.println("varName error");
        }
        return null;
    }

    /**
     * Method that runs after running the parse tree
     */
    @Override
    public void runAtEnd() {

    }
}
