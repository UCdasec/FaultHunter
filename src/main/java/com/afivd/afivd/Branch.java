package com.afivd.afivd;

import java.util.*;
import org.antlr.v4.runtime.Token;

/**
 * The Branch class checks for trivial constants in if-expressions to better safeguard against fault injection attacks
 * Covers Fault.BRANCH
 */
public class Branch extends CBaseListener{

    // Private Variables
    private boolean currentlyInIfStatement;
    private boolean inOrExpression;
    private boolean inAndExpression;

    // Temporary Storage for nested ANDs and ORs
    private ArrayList<TempResult> tempResults = new ArrayList<>();

    // Output Array
    private ParsedResults output;

    /**
     * Branch pattern Constructor requires the Parser and the output storage, ParsedResults
     * @param output A ParsedResults storage object to be appended to
     */
    public Branch(ParsedResults output) {
        this.currentlyInIfStatement = false;
        this.output = output;
    }

    /**
     * TempResult is a private inner class used to store Branch results specifically in the case when OR or AND statements
     * are present. This helper class assists in determining if the overall statement will be considered trivial or not.
     */
    private static class TempResult{
        public static final short AND_FLAG = 0b00000000;
        public static final short OR_FLAG = 0b00000001;
        private final boolean isTrivial;
        private final short selectionContextFlag;
        private final ResultLine resultLine;
        public TempResult(boolean isTrivial, short selectionContextFlag, ResultLine resultLine){
            this.isTrivial = isTrivial;
            this.selectionContextFlag = selectionContextFlag;
            this.resultLine = resultLine;
        }
        public boolean isTrivial() {return isTrivial;}
        public short getSelectionContextFlag() {return selectionContextFlag;}
        public ResultLine getResultLine() {return resultLine;}
    }

    // ------------------------------------------ Listener Overrides ---------------------------------------------------
    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        if (ctx.If() != null) {currentlyInIfStatement = true;}
        // Clear TempResults for use if the if-statement has OR or AND statements
        tempResults.clear();
    }
    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        if (ctx.If() != null) {currentlyInIfStatement = false;}
        // Now, before we leave the if-statement, lets see if we have a stored number of TempResults that we may need to report on
        int numTrivialORStatements=0;
        int numTrivialANDStatements=0;
        int numNonTrivialORStatements=0;
        int numNonTrivialANDStatements=0;
        for(TempResult tempResult : tempResults){
            if(tempResult.isTrivial()){
                if(tempResult.getSelectionContextFlag() == TempResult.OR_FLAG){
                    numTrivialORStatements++;
                }else if(tempResult.getSelectionContextFlag() == TempResult.AND_FLAG){
                    numTrivialANDStatements++;
                }
            }else{
                if(tempResult.getSelectionContextFlag() == TempResult.OR_FLAG){
                    numNonTrivialORStatements++;
                }else if(tempResult.getSelectionContextFlag() == TempResult.AND_FLAG){
                    numNonTrivialANDStatements++;
                }
            }
        }
        // Report if the number of trivial / non-trivial statements is correct
        // Will be very verbose with these if-statements to make sure I handle all of the cases
        // Only AND statements:
        if(numNonTrivialORStatements==0&&numTrivialORStatements==0&&numTrivialANDStatements>=1&&numNonTrivialANDStatements==0){
            for(TempResult tempResult : tempResults){
                if(tempResult.isTrivial()){
                    output.appendResult(tempResult.getResultLine());
                }
            }
        // Only OR statements
        }else if(numTrivialANDStatements==0&&numNonTrivialANDStatements==0&&numTrivialORStatements>=1){
            for(TempResult tempResult : tempResults){
                if(tempResult.isTrivial()){
                    output.appendResult(tempResult.getResultLine());
                }
            }
        // One OR Statement, many AND statements (AND are by default evaluated first)
        // One AND Statement, many OR statements (AND are by default evaluated first)
        }else if(numNonTrivialORStatements==0&&numNonTrivialANDStatements==0){
            for(TempResult tempResult : tempResults){
                if(tempResult.isTrivial()){
                    output.appendResult(tempResult.getResultLine());
                }
            }
        }else if(numNonTrivialORStatements>0&&numTrivialORStatements==0&&numTrivialANDStatements>=1&&numNonTrivialANDStatements==0){
            for(TempResult tempResult : tempResults){
                if(tempResult.isTrivial()){
                    output.appendResult(tempResult.getResultLine());
                }
            }
        }
        else if(numNonTrivialORStatements==0&&numTrivialORStatements>0){
            for(TempResult tempResult : tempResults){
                if(tempResult.isTrivial()){
                    output.appendResult(tempResult.getResultLine());
                }
            }
        }

        tempResults.clear();
    }

    // When we enter a logicalOrExpressions, and there is more than one logicalAndExpression, this means that there is an
    // OR operator in use. It is then possible for there to be an AND expression present, as noted by more than one
    // inclusiveOrExpression. If we have a logicalOrExpression with more than one logicalAndExpression, we should mark
    // 'inOrExpression' as true, then, if we have a logicalAndExpression with more than one inclusiveOrExpression, we should
    // mark 'inAndExpression' as true. Boolean logic can then be used in our further functions, as follows:

    //      * inOrExpression is true and inAndExpression is false (inside a simple OR statement)
    //              o If at least one expression is trivial, report the line
    //      * inOrExpression is false and inAndExpression is true (inside a simple AND statement)
    //              o All expressions must be trivial to report the line
    //      * inOrExpression is true and inAndExpression is true
    //              o Wait for result of AND expressions, then test result with OR expression before reporting
    //      * inOrExpression is false and inAndExpression is false
    //              o Will be able to make results like normal

    // A new data structure to hold the results of 'trivialness' when evaluating equality and relational expressions is
    // needed to then also be able to hold the context of AND and OR expressions along with them. In our case, this context
    // will simply be counting the number of OR statements that are deemed trivial and the number of AND statements that are
    // deemed trivial.
    //      * >= 1 OR statements are deemed trivial and there are 0 AND statements
    //              o Overall trivial
    //      * 0 OR statements and all AND statements deemed trivial
    //              o Overall trivial
    //      * >= 1 OR statements deemed trivial and all AND statements deemed trivial
    //              o Overall trivial

    @Override
    public void enterLogicalOrExpression(CParser.LogicalOrExpressionContext ctx) {
        if(ctx.logicalAndExpression().size()>1){this.inOrExpression=true;}
    }
    @Override
    public void exitLogicalOrExpression(CParser.LogicalOrExpressionContext ctx) {this.inOrExpression=false;}
    @Override
    public void enterLogicalAndExpression(CParser.LogicalAndExpressionContext ctx) {
        if(ctx.inclusiveOrExpression().size()>1){this.inAndExpression=true;}
    }
    @Override
    public void exitLogicalAndExpression(CParser.LogicalAndExpressionContext ctx) {this.inAndExpression=false;}

    // Just for equal to (==)
    @Override
    public void enterEqualityExpression(CParser.EqualityExpressionContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        List<CParser.RelationalExpressionContext> ctxes = ctx.relationalExpression();
        if (ctxes.size() > 1) {
            if (ctx.Equal() != null && currentlyInIfStatement) {
                if (ctxes.get(1).getText().equalsIgnoreCase("true") || ctxes.get(1).getText().equalsIgnoreCase("false")) {
                    if(inOrExpression&&!inAndExpression){
                        tempResults.add(new TempResult(true,TempResult.OR_FLAG,new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using trivial bool in branch statement.",lineNumber)));
                    }else if (!inOrExpression&&inAndExpression){
                        tempResults.add(new TempResult(true,TempResult.AND_FLAG,new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using trivial bool in branch statement.",lineNumber)));
                    }else if (inOrExpression&&inAndExpression){
                        tempResults.add(new TempResult(true,TempResult.AND_FLAG,new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using trivial bool in branch statement.",lineNumber)));
                    }else{
                        // Else, the if statement does not use an AND or an OR operator
                        output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using trivial bool in branch statement.",lineNumber));
                    }


                } else if (isInteger(ctxes.get(1).getText())) {
                    // Code commented out and replaced with result creation to trigger on every explicit integer
                    /*
                    int number = Integer.parseInt(ctx.relationalExpression(1).getText());
                    if (number == 0 || number == 1) {output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"branch",ctx.getText()+" Using bool.",lineNumber));}
                     */
                    if(inOrExpression&&!inAndExpression){
                        tempResults.add(new TempResult(true,TempResult.OR_FLAG,new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using explicit integer instead of variable in branch.",lineNumber)));
                    }else if (!inOrExpression&&inAndExpression){
                        tempResults.add(new TempResult(true,TempResult.AND_FLAG,new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using explicit integer instead of variable in branch.",lineNumber)));
                    }else if (inOrExpression&&inAndExpression){
                        tempResults.add(new TempResult(true,TempResult.AND_FLAG,new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using explicit integer instead of variable in branch.",lineNumber)));
                    }else{
                        // Else, the if statement does not use an AND or an OR operator
                        output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using explicit integer instead of variable in branch.",lineNumber));
                    }
                } else {
                    // Else, the statement is deemed not trivial, so if we are in an AND or OR statement, report in the negative
                    // ResultLines will be null since we won't use them anyways
                    if(inOrExpression&&!inAndExpression){
                        tempResults.add(new TempResult(false,TempResult.OR_FLAG,null));
                    }else if (!inOrExpression&&inAndExpression){
                        tempResults.add(new TempResult(false,TempResult.AND_FLAG,null));
                    }else if (inOrExpression&&inAndExpression){
                        tempResults.add(new TempResult(false,TempResult.AND_FLAG,null));
                    }
                    // Else, it is a simple, non-trivial if-statement without ORs or ANDs.
                }
            }
        }
    }

    // For less than or equal to (<=) and greater than or equal to (>=)
    @Override
    public void enterRelationalExpression(CParser.RelationalExpressionContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        List<CParser.ShiftExpressionContext> ctxes = ctx.shiftExpression();
        if (ctxes.size() > 1) {
            if (ctx.GreaterEqual() != null || ctx.LessEqual() != null && currentlyInIfStatement) {
                if (isInteger(ctxes.get(1).getText())) {
                    // Code commented out and replaced with result creation to trigger on every explicit integer
                    /*
                    int number = Integer.parseInt(ctx.relationalExpression(1).getText());
                    if (number == 0 || number == 1) {output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"branch",ctx.getText()+" Using bool.",lineNumber));}
                     */
                    if(inOrExpression&&!inAndExpression){
                        tempResults.add(new TempResult(true,TempResult.OR_FLAG,new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using explicit integer instead of variable in branch.",lineNumber)));
                    }else if (!inOrExpression&&inAndExpression){
                        tempResults.add(new TempResult(true,TempResult.AND_FLAG,new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using explicit integer instead of variable in branch.",lineNumber)));
                    }else if (inOrExpression&&inAndExpression){
                        tempResults.add(new TempResult(true,TempResult.AND_FLAG,new ResultLine(ResultLine.SINGLE_LINE,"branch","\""+ctx.getText()+"\""+" Using explicit integer instead of variable in branch.",lineNumber)));
                    }else {
                        output.appendResult(new ResultLine(ResultLine.SINGLE_LINE, "branch", "\""+ctx.getText() +"\""+ " Using explicit integer instead of variable in branch.", lineNumber));
                    }
                }
                // Else, the statement is deemed not trivial, so if we are in an AND or OR statement, report in the negative
                // ResultLines will be null since we won't use them anyways
                if(inOrExpression&&!inAndExpression){
                    tempResults.add(new TempResult(false,TempResult.OR_FLAG,null));
                }else if (!inOrExpression&&inAndExpression){
                    tempResults.add(new TempResult(false,TempResult.AND_FLAG,null));
                }else if (inOrExpression&&inAndExpression){
                    tempResults.add(new TempResult(false,TempResult.AND_FLAG,null));
                }
                // Else, it is a simple, non-trivial if-statement without ORs or ANDs.
            }
        }
    }

    // -------------------------------------------- Helper Functions ---------------------------------------------------
    // TODO: Use a more efficient integer checking function later
    private boolean isInteger(String str) {
        return str.matches("-?\\d+");
    }
}

