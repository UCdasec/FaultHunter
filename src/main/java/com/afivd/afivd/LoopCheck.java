package com.afivd.afivd;

public class LoopCheck extends CBaseListener{
    private ParsedResults output;
    private boolean inForLoop = false;

    public LoopCheck(ParsedResults output){this.output = output;}

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        if(ctx.getStart().getText().equalsIgnoreCase("for")){
            this.inForLoop = true;
        }
    }
    @Override
    public void exitIterationStatement(CParser.IterationStatementContext ctx) {
        if(ctx.getStart().getText().equalsIgnoreCase("for")){
            this.inForLoop = false;
        }
    }

    // Collect the for-expression that has the conditional check. Then, check after the for-loop for a conditional that has the same
    // conditional check (to ensure that LoopCheck hasn't already been coded). If not, you will need to then pull out the variable of
    // for-loop (if is a new declaration) and put a layer above in scope. Afterward, make the if-statement right after the for-loop
    // with the previously collected conditional check from the for-loop.


}
