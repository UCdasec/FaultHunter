package com.afivd.afivd;

import java.util.*;
import org.antlr.v4.runtime.Token;

/**
 * The Branch class checks for trivial constants in if-expressions to better safeguard against fault injection attacks
 * Covers Fault.BRANCH
 */
public class Branch extends CBaseListener{
    // Private Variables
    private CParser parser;
    private boolean currentlyInIfStatement;

    // Parser results, correlated by same index value
    // TODO: Look at this later, currently unused
    private List<Integer> lineNumbers = new ArrayList<>();
    private List<String> expressionContent = new ArrayList<>();
    private List<Integer> values = new ArrayList<>();

    // Output Array
    private ParsedResults output;

    /**
     * Branch pattern Constructor requires the Parser and the output storage, ParsedResults
     * @param parser The CParser object
     * @param output A ParsedResults storage object to be appended to
     */
    public Branch(CParser parser, ParsedResults output) {
        this.parser = parser;
        this.currentlyInIfStatement = false;
        this.output = output;
    }


    // ------------------------------------------ Listener Overrides ---------------------------------------------------
    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        if (ctx.If() != null) {
            currentlyInIfStatement = true;
        }
    }
    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        if (ctx.If() != null) {
            currentlyInIfStatement = false;
        }
    }
    @Override
    public void enterEqualityExpression(CParser.EqualityExpressionContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        List<CParser.RelationalExpressionContext> ctxes = ctx.relationalExpression();
        if (ctxes.size() > 1) {
            if (ctx.Equal() != null && currentlyInIfStatement) {
                // Change to equalsIgnoreCase
                if (ctxes.get(1).getText().equalsIgnoreCase("true") || ctxes.get(1).getText().equalsIgnoreCase("false")) {
                    output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"BRANCH: "+ctx.getText()+" Using bool.",lineNumber));

                } else if (isInteger(ctxes.get(1).getText())) {
                    int number = Integer.parseInt(ctx.relationalExpression(1).getText());
                    if (number == 0 || number == 1) {
                        output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"BRANCH: "+ctx.getText()+" Using bool.",lineNumber));
                    }
                }
            }
        }
    }

    // -------------------------------------------- Helper Functions ---------------------------------------------------
    private boolean isInteger(String str) {
        return str.matches("-?\\d+");
    }
}

