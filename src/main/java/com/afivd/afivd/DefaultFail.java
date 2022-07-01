package com.afivd.afivd;

import org.antlr.v4.runtime.Token;

/**
 * Flags else and default blocks in the parsed C code as potentially unsafe (in the terms of fault injection resistance
 * Covers Fault.DEFAULTFAIL
 */
public class DefaultFail extends CBaseListener implements FaultPattern{
    private final ParsedResults output;

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
            this.output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"default_fail",ctx.getText()+" uses potentially unsafe default statement. ",lineNumber));
        }
    }

    // Listener to catch 'else{...}' code blocks
    // Thought-process: When entering a selectionStatement, look for an else token at the third position. If there is an else token,
    // look into the statement at the fourth position, and go as deep as possible
    private int depthMeter    = 0;
    private int maxDepthMeter = 0;
    private CParser.SelectionStatementContext selectionStatementContext;
    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        this.depthMeter++;
        if(this.depthMeter>this.maxDepthMeter){
            this.maxDepthMeter = this.depthMeter;
            this.selectionStatementContext = ctx;
        }
    }
    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        this.depthMeter--;
    }

    // -------------------------------------------- Helper Functions ---------------------------------------------------
    @Override
    public void runAtEnd(){
        if(selectionStatementContext != null && selectionStatementContext.Else() != null){
            Token token = this.selectionStatementContext.Else().getSymbol();
            int lineNumber = token.getLine();
            this.output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"default_fail","\""+selectionStatementContext.Else().getText()+"\""+" uses potentially unsafe else statement. ",lineNumber));
        }
    }


}
