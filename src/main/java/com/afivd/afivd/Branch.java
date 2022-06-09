package com.afivd.afivd;
// This detects if a boolean value is being used for a decision
//

import java.util.*;
import org.antlr.v4.runtime.Token;

public class Branch extends CBaseListener {
    CParser parser;
    boolean currentlyInIfStatement;
    ArrayList<String> output;

    List<Integer> lineNumbers = new ArrayList<Integer>();
    List<String> ctxes = new ArrayList<String>();
    List<Integer> values = new ArrayList<Integer>();

    public Branch(CParser parser, ArrayList<String> output) {
        this.parser = parser;
        this.currentlyInIfStatement = false;
        this.output = output;
    }

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
        StringBuilder stringBuilder = new StringBuilder();
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        List<CParser.RelationalExpressionContext> ctxes = ctx.relationalExpression();
        if (ctxes.size() > 1) {
            if (ctx.Equal() != null && currentlyInIfStatement) {
                if (ctxes.get(1).getText().equals("true") || ctxes.get(1).getText().equals("false")) {

                    stringBuilder.append("Line ").append(lineNumber).append(": ").append(ctx.getText()).append(" Using bool.");
                } else if (isInteger(ctxes.get(1).getText())) {
                    int number = Integer.parseInt(ctx.relationalExpression(1).getText());
                    if (number == 0 || number == 1) {
                        stringBuilder.append(lineNumber).append(": ").append(ctx.getText()).append(" Using bool.");
                    }
                }
            }
        }
        output.add(stringBuilder.toString());
    }

    private boolean isInteger(String str) {
        return str.matches("-?\\d+");
    }

}

