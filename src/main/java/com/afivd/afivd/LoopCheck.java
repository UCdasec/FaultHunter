package com.afivd.afivd;

import java.util.ArrayList;

/**
 * LoopCheck is a replacement pattern that adds an if-statement after a for-loop to ensure that it completed successfully.
 * Additionally, it will automatically rename the iterator variable if another variable with the same name is in the same
 * or higher scope.
 *      o May not work with for-loops that contain breaks or continues.
 */
public class LoopCheck extends CBaseListener implements FaultPattern{
    private ParsedResults output;
    private ArrayList<VariableSearcher.VariableTuple> variables;
    private boolean inForLoop = false;

    public LoopCheck(ParsedResults output,ArrayList<VariableSearcher.VariableTuple> variables){
        this.variables = variables;
        this.output = output;
    }
    // ------------------------------------------ Listener Overrides ---------------------------------------------------
    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        if(ctx.getStart().getText().equalsIgnoreCase("for")){
            // Plan of replacement is probably {variable initialization}+\n+
            // normal for-loop with modified initializer +\n+
            // {if statement for fault detect}
            // We know the position and line of the start and end, so we can just do character replacement probably
            //System.out.println(ctx.start.getText()+":"+ctx.start.getLine()+":"+ctx.start.getCharPositionInLine()+" | "+ctx.stop.getText()+":"+ctx.stop.getLine()+":"+ctx.stop.getCharPositionInLine());


        }
    }


    // -------------------------------------------- Helper Functions ---------------------------------------------------
    /**
     * Method that runs after running the parse tree
     */
    @Override
    public void runAtEnd() {

    }

    // Collect the for-expression that has the conditional check. Then, check after the for-loop for a conditional that has the same
    // conditional check (to ensure that LoopCheck hasn't already been coded). If not, you will need to then pull out the variable of
    // for-loop (if is a new declaration) and put a layer above in scope. Afterward, make the if-statement right after the for-loop
    // with the previously collected conditional check from the for-loop.
    //  Ex: for(int i = 0; i < 10; i++){... would have after the for-loop "if(i<10){faultDetect())"
    //      o This is because the for-loop only runs while the if-statement evaluates to true. It should be false afterward



}
