package com.afivd.afivd;

import org.antlr.v4.runtime.Token;

public class DefaultFail extends CBaseListener{
    private ParsedResults output;

    public DefaultFail(ParsedResults output){
        this.output = output;
    }

    // ------------------------------------------ Listener Overrides ---------------------------------------------------
    // Listener to catch 'default:' code blocks
    @Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        if(ctx.start.getText().equalsIgnoreCase("default")){
            output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"default_fail",ctx.getText()+" uses potentially unsafe default statement. ",lineNumber));
        }
    }

    // Listener to catch 'else{...}' code blocks
    // Thought-process: When entering a selectionStatement, look for an else token at the third position. If there is an else token,
    // look into the statement at the fourth position, and go as deep as possible
    private int depthMeter = 0;
    private int maxDepthMeter = 0;
    private CParser.SelectionStatementContext selectionStatementContext;
    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        depthMeter++;
        if(depthMeter>maxDepthMeter){
            maxDepthMeter = depthMeter;
            selectionStatementContext = ctx;
        }
    }
    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        depthMeter--;
    }

    // -------------------------------------------- Helper Functions ---------------------------------------------------
    public void runAtEnd(){
        if(selectionStatementContext.Else() != null){
            Token token = selectionStatementContext.Else().getSymbol();
            int lineNumber = token.getLine();
            output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"default_fail","\""+selectionStatementContext.Else().getText()+"\""+" uses potentially unsafe else statement. ",lineNumber));
        }
    }


}
