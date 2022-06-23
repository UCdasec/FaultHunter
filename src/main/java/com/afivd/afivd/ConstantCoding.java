package com.afivd.afivd;

// The objective is to locate any instance of a variable being assigned
// a value (i.e. x = 5) and evaluate the hamming weight of this value.
// Additionally, we want to catch trivial constants like 0x00 and 0xFF

import java.util.*;  
import org.antlr.v4.runtime.Token;

/**
 * The ConstantCoding class checks for trivial constants that may pose as a risk in terms of fault injection attacks.
 * Currently, locates variables being assigned a value and being initialized with a value
 * Covers Fault.CONSTANTCODING
 */
public class ConstantCoding extends CBaseListener {
    // Private Variables
    private CParser parser;
    private final int sensitivity;
    private boolean inForLoop = false;
    private ParsedResults output;

    // Parser Results, correlated by same index value
    private List<Integer> lineNumbers = new ArrayList<>();
    private List<String> expressionContent = new ArrayList<>();
    private List<Integer> values = new ArrayList<>();

    /**
     * ConstantCoding Constructor requires the CParser object, output storage ParsedResults, and a hamming sensitivity
     * @param parser The CParser Object
     * @param output A ParsedResults storage object to be appended to
     * @param sensitivity The Hamming Distance between two constants that will start to trigger our notification message
     */
    public ConstantCoding(CParser parser, ParsedResults output, int sensitivity) {
        this.parser = parser;
        this.sensitivity = sensitivity;
        this.output = output;
    }

    // We are now looking inside functions for constant usage and declaration
    /* These two functions not currently needed
    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {inFunctionDefinition = true;}
    @Override
    public void exitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {inFunctionDefinition = false;}
     */

    // ------------------------------------------ Listener Overrides ---------------------------------------------------

    @Override
    public void enterInitDeclarator(CParser.InitDeclaratorContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        int number;
        // Current exception list:
        //      * No variables in for-loop declaration
        if (ctx.initializer() != null && !inForLoop && isInteger(ctx.initializer().getText())) {
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
            expressionContent.add(ctx.getText());
            values.add(number);
        }
    }
    @Override 
    public void enterAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        int number;
        // Current exception list:
        //      * No variables in for-loop declaration
        if (ctx.assignmentOperator() != null && !inForLoop && ctx.assignmentOperator().getText().equals("=")
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
            expressionContent.add(ctx.getText());
            values.add(number);
        }
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        this.inForLoop = true;
    }
    @Override
    public void exitIterationStatement(CParser.IterationStatementContext ctx) {
        this.inForLoop = false;
    }

    // TODO: Add override code to also look at "#define" constants as well. Note, define statements are not part of the
    //  current grammar file for some reason

    // -------------------------------------------- Helper Functions ---------------------------------------------------

    /**
     * CalculateHamming calculates the Hamming Distance between the passed number and zero
     * @param x Number to calculate Hamming Distance
     * @return Hamming distance between the passed number and zero
     */
    private int calculateHamming(int x) {
        int count = 0;
        while (x != 0) {
            x = x & (x-1);
            count = count + 1;
        }
        return count;
    }

    /**
     * CompareHamming calculates the hamming distance between the passed two numbers
     * @param a Integer number
     * @param b Integer number
     * @return The Hamming Distance between the two passed numbers
     */
    private int compareHamming(int a, int b) {
        int count = 0;
        int x = a ^ b;
        while (x != 0) {
            count = count + 1;
            x = x & (x-1);
        }
        return count;
    }

    /**
     * isInteger takes a given string and determines if it represents an integer.
     * @param str The string to be tested
     * @return True if the passed string is an integer, false otherwise.
     */
    private boolean isInteger(String str) {return str.matches("-?(0x)?[\\p{XDigit}]+");}

    /**
     * isHex takes a passed string and determines if it is in hexadecimal format
     * @param str The string to be tested
     * @return True if the passed string is a hex string, false otherwise.
     */
    private boolean isHex(String str) {return str.matches("0x[\\p{XDigit}]+");}

    public void analyze() {
        // First, search value list for trivial constants such as 0xFF and 0x0
        // TODO: For now, detect all constant integers that are declared. A better system will check to see if these values are ever
        //  modified by code, and thus can be ignored by the constant coding pattern
        for (int i = 0; i < values.size(); i++) {
            int value = values.get(i);
            switch (value) {
                case 0x00:
                    output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"constant_coding","(Trivial): "+ expressionContent.get(i)+" has value of 0x00.\n\tConsider replacement.",lineNumbers.get(i)));
                    break;
                case 0x01:
                    output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"constant_coding","(Trivial): "+ expressionContent.get(i)+" has value of 0x01.\n\tConsider replacement.",lineNumbers.get(i)));
                    break;
                case 0xFF:
                    output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"constant_coding","(Trivial): "+ expressionContent.get(i)+" has value of 0xFF.\n\tConsider replacement.",lineNumbers.get(i)));
                    break;
                default:
                    break;
            }
        }

        // TODO: Revise when hamming distance between values is calculated (probably just inside switch statements)
        // For remaining values, calculate the Hamming distance between them and warn user if below sensitivity value
        for (int i = 0; i < values.size() - 1; i++) {
            for (int j = i + 1; j < values.size(); j++) {
                int hamming = compareHamming(values.get(i), values.get(j));
                if (hamming <= sensitivity) {
                    output.appendResult(new ResultLine(ResultLine.MULTI_LOCATION,"constant_coding","(Low Hamming): Lines "+lineNumbers.get(i)+" : "+ expressionContent.get(i)+" and "+lineNumbers.get(j)+" : "+ expressionContent.get(j)+" have a low Hamming distance ("+hamming+").\n\tConsider replacement.",lineNumbers.get(i),lineNumbers.get(j)));
                }
            }
        }
    }
}
