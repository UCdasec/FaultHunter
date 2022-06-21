package com.afivd.afivd;

import java.util.*;
import org.antlr.v4.runtime.Token;

/**
 * The Branch class checks for trivial constants in if-expressions to better safeguard against fault injection attacks
 * Covers Fault.BRANCH
 */
public class Branch extends CBaseListener{
    // TODO: Look at the unused variables in this class and determine if we will need these

    // Private Variables
    private CParser parser;
    private boolean currentlyInIfStatement;

    // Parser results, correlated by same index value

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

    // TODO: Redo to use ConditionalExpressionContext rather than EqualityExpressionContext, to get less than and greater than
    @Override
    public void enterEqualityExpression(CParser.EqualityExpressionContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        List<CParser.RelationalExpressionContext> ctxes = ctx.relationalExpression();

        // TODO: Add code to look at difference between AND'd and OR'd statements, will most likely have to modify hardcoded 1 value
        if (ctxes.size() > 1) {

            if (ctx.Equal() != null && currentlyInIfStatement) {
                System.out.println(ctx.getText());
                if (ctxes.get(1).getText().equalsIgnoreCase("true") || ctxes.get(1).getText().equalsIgnoreCase("false")) {
                    output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"branch",ctx.getText()+" Using bool.",lineNumber));

                } else if (isInteger(ctxes.get(1).getText())) {
                    int number = Integer.parseInt(ctx.relationalExpression(1).getText());
                    // TODO: Potentially change this so that if the if statement contains an explicit integer we flag it
                    if (number == 0 || number == 1) {
                        output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"branch",ctx.getText()+" Using bool.",lineNumber));
                    }
                }
            }
        }
    }

    // -------------------------------------------- Helper Functions ---------------------------------------------------
    // TODO: Use a more efficient integer checking function later
    private boolean isInteger(String str) {
        return str.matches("-?\\d+");
    }
}

