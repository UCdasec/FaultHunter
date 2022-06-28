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


}
