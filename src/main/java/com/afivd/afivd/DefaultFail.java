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
    // Listener to catch 'default:' code blocks with only a break in them TODO: ensure that if there is code, not to remove from testing!
    @Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        if(ctx.start.getText().equalsIgnoreCase("default") && ctx.statement().jumpStatement() != null && ctx.statement().jumpStatement().getStart() != null && !ctx.statement().jumpStatement().getStart().getText().equalsIgnoreCase("break")){
            this.output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"default_fail",ctx.getText()+" uses potentially unsafe default statement. ",lineNumber));
        }
    }

    // Listener to catch 'else{...}' code blocks
    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        if(ctx.Else() != null && ctx.statement().get(1)!=null){
            if(ctx.statement().get(1).selectionStatement()!=null){
                // Do nothing, this is an else-if statement
            }else if(ctx.statement().get(1).compoundStatement() != null || ctx.statement().get(1).expressionStatement() != null){
                // At this point we should be inside an else body
                // TODO: If there is only a return here we could assume that it is not default fail (Ex: default: \n return;)
                this.output.appendResult(new ResultLine(ResultLine.SINGLE_LINE, "default_fail", "\"" + ctx.Else().getText() + "\"" + " uses potentially unsafe else statement. ", ctx.Else().getSymbol().getLine()));
            }
        }
    }


    // -------------------------------------------- Helper Functions ---------------------------------------------------
    @Override
    public void runAtEnd () {
        // Nothing currently needed for DefaultFail
    }
}