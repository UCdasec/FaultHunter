package com.afivd.afivd;

// The objective is to locate any instance of a variable being assigned
// a value (i.e. x = 5) and evaluate the hamming weight of this value.
// Additionally, we want to catch trivial constants like 0x00 and 0xFF

import java.util.*;  
import org.antlr.v4.runtime.Token;

public class ConstantCoding extends CBaseListener {
    CParser parser;
    int sensitivity;
    boolean inFunctionDefinition;
    ArrayList<String> output;

    // Since Java doesn't have a built in tuple class, I'm using three lists
    // to keep track of the information we need. The information will be matched 
    // by the index.
    List<Integer> lineNumbers = new ArrayList<>();
    List<String> ctxes = new ArrayList<>();
    List<Integer> values = new ArrayList<>();


    public ConstantCoding(CParser parser, int sensitivity, ArrayList<String> output) {
        this.parser = parser;
        this.sensitivity = sensitivity;
        inFunctionDefinition = false;
        this.output = output;
    }

    // Globals are outside of main, when we enter a function defintion we no
    // longer want to add constants to our list

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        inFunctionDefinition = true;
    }


    @Override
    public void exitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        inFunctionDefinition = false;
    }

    @Override
    public void enterInitDeclarator(CParser.InitDeclaratorContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        int number;
        if (inFunctionDefinition == false && ctx.initializer() != null && isInteger(ctx.initializer().getText())) {

            try {
                    if (isHex(ctx.initializer().getText())) {
                        number = Integer.parseInt(ctx.initializer().getText().replaceAll("0x", ""), 16);
                    } else {
                        number = Integer.parseInt(ctx.initializer().getText());
                    }
            } catch (NumberFormatException e) {
                return;
            }
            lineNumbers.add(lineNumber);
            ctxes.add(ctx.getText());
            values.add(number);
        }
    }

    @Override 
    public void enterAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        int number;
        if (inFunctionDefinition == false && ctx.assignmentOperator() != null && ctx.assignmentOperator().getText().equals("=")
                && ctx.assignmentExpression() != null && isInteger(ctx.assignmentExpression().getText())) {
                try {
                    if (isHex(ctx.assignmentExpression().getText())) {
                        number = Integer.parseInt(ctx.assignmentExpression().getText().replaceAll("0x", ""), 16);
                    } else {
                        number = Integer.parseInt(ctx.assignmentExpression().getText());
                    }
                } catch (NumberFormatException e) {
                    return;
                }
            lineNumbers.add(lineNumber);
            ctxes.add(ctx.getText());
            values.add(number);
        }
    }
    
    // Returns the number of differing bits between x and 0
    private int calculateHamming(int x) {
        int count = 0;
        while (x != 0) {
            x = x & (x-1);
            count = count + 1;
        }
        return count;
    }
    
    // Returns the number of differing bits between a and b
    private int compareHamming(int a, int b) {
        int count = 0;
        int x = a ^ b;
        while (x != 0) {
            count = count + 1;
            x = x & (x-1);
        }
        return count;
    }
    
    // Tests if given string could be an integer or not
    private boolean isInteger(String str) {
        return str.matches("-?(0x)?[\\p{XDigit}]+");
    }
    
    // Tests if string is hex
    private boolean isHex(String str) {
        return str.matches("0x[\\p{XDigit}]+");
    }

    public void analyze() {
        // First, search value list for trivial constants such as 0xFF and 0x0
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nTrivial constants\n");
        stringBuilder.append("----------------------------\n");
        for (int i = 0; i < values.size(); i++) {
            int value = values.get(i);
            switch (value) {
                case 0x00:
                    stringBuilder.append("Line ").append(lineNumbers.get(i)).append(": ").append(ctxes.get(i)).append(" has value of 0x00.\nIf this is a sensitive value, consider revising.\n");
                    break;
                case 0x01:
                    stringBuilder.append("Line ").append(lineNumbers.get(i)).append(": ").append(ctxes.get(i)).append(" has value of 0x01.\nIf this is a sensitive value, consider revising.\n");
                    break;
                case 0xFF:
                    stringBuilder.append("Line ").append(lineNumbers.get(i)).append(": ").append(ctxes.get(i)).append(" has value of 0xFF.\nIf this is a sensitive value, consider revising.\n");
                    break;
                default:
                    break;
            }
        }
        
        // For remaining values, calculate the Hamming distance between them and
        // warn user if below threshold value
        stringBuilder.append("\nHamming weights\n");
        stringBuilder.append("----------------------------\n");
        for (int i = 0; i < values.size() - 1; i++) {
            for (int j = i + 1; j < values.size(); j++) {
                int hamming = compareHamming(values.get(i), values.get(j));
                if (hamming <= sensitivity) {
                    stringBuilder.append("Line ").append(lineNumbers.get(i)).append(": ").append(ctxes.get(i)).append(" and line ").append(lineNumbers.get(j)).append(": ").append(ctxes.get(j)).append(" have Hamming distance of ").append(hamming).append(".\n");
                    stringBuilder.append("If these are sensitive constants, consider revising.\n");
                }
            }
        }
        output.add(stringBuilder.toString());
    }
}
