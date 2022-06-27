package com.afivd.afivd;

import java.util.*;  
import org.antlr.v4.runtime.Token;

/**
 * The ConstantCoding class checks for trivial constants that may pose as a risk in terms of fault injection attacks.
 * Currently, locates variables being assigned a value and being initialized with a value
 * Covers Fault.CONSTANTCODING
 */
public class ConstantCoding extends CBaseListener {
    // Private Variables
    private final int sensitivity;
    private boolean inForLoop = false;
    private boolean inSwitchCase = false;
    private ParsedResults output;

    // TODO: replace this with inner class: these lists are sort of messy
    // Parser Results, correlated by same index value
    private List<Integer> lineNumbers = new ArrayList<>();
    private List<String> expressionContent = new ArrayList<>();
    private List<Integer> values = new ArrayList<>();

    // Specific lists for values inside switch statement cases only
    private List<Integer> switchLineNumbers = new ArrayList<>();
    private List<String> switchExpressionContent = new ArrayList<>();
    private List<Integer> switchValues = new ArrayList<>();

    /**
     * ConstantCoding Constructor requires the CParser object, output storage ParsedResults, and a hamming sensitivity
     * @param output A ParsedResults storage object to be appended to
     * @param sensitivity The Hamming Distance between two constants that will start to trigger our notification message
     */
    public ConstantCoding(ParsedResults output, int sensitivity) {
        //this.parser = parser;
        this.sensitivity = sensitivity;
        this.output = output;
    }

    // We are now looking inside functions for constant usage and declaration
    // These two functions not currently needed 'enterFunctionDefinition' and 'exitFunctionDefinition'

    // ------------------------------------------ Listener Overrides ---------------------------------------------------
    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        this.inForLoop = true;
    }
    @Override
    public void exitIterationStatement(CParser.IterationStatementContext ctx) {
        this.inForLoop = false;
    }
    @Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx){
        this.inSwitchCase = true;
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        int number;
        if (ctx.constantExpression() != null && isInteger(ctx.constantExpression().getText())){
            try {
                if (isHex(ctx.constantExpression().getText())) {
                    number = Integer.parseInt(ctx.constantExpression().getText().replaceAll("0x", ""), 16);
                } else {
                    number = Integer.parseInt(ctx.constantExpression().getText());
                }
            } catch (NumberFormatException e) {
                return;
            }

            switchValues.add(number);
            switchLineNumbers.add(lineNumber);
            switchExpressionContent.add(ctx.getText());

        }

    }
    @Override
    public void exitLabeledStatement(CParser.LabeledStatementContext ctx){
        this.inSwitchCase = false;
    }

    @Override
    public void enterInitDeclarator(CParser.InitDeclaratorContext ctx) {
        Token token = ctx.getStart();
        int lineNumber = token.getLine();
        int number;
        // Current exception list:
        //      * No variables in for-loop declaration
        if (ctx.initializer() != null && !inForLoop && isInteger(ctx.initializer().getText())) {
            // TODO: Add detection of boolean constants
            try {
                    if (isHex(ctx.initializer().getText())) {
                        number = Integer.parseInt(ctx.initializer().getText().replaceAll("0x", ""), 16);
                    } else {
                        number = Integer.parseInt(ctx.initializer().getText());
                    }


            } catch (NumberFormatException e) {System.out.println("something wrong with regex");return;}

            // Case statements will be handled differently than normal constants
            if(!inSwitchCase) {
                lineNumbers.add(lineNumber);
                expressionContent.add(ctx.getText());
                values.add(number);
            }

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

            // Case statements will be handled differently than normal constants
            if(!inSwitchCase) {
                lineNumbers.add(lineNumber);
                expressionContent.add(ctx.getText());
                values.add(number);
            }

        }
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
                    output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"constant_coding","(Trivial): "+ "\""+expressionContent.get(i)+"\""+" has value of 0x00.\n\tConsider replacement.",lineNumbers.get(i)));
                    break;
                case 0x01:
                    output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"constant_coding","(Trivial): "+ "\""+expressionContent.get(i)+"\""+" has value of 0x01.\n\tConsider replacement.",lineNumbers.get(i)));
                    break;
                case 0xFF:
                    output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"constant_coding","(Trivial): "+ "\""+expressionContent.get(i)+"\""+" has value of 0xFF.\n\tConsider replacement.",lineNumbers.get(i)));
                    break;
                default:
                    output.appendResult(new ResultLine(ResultLine.SINGLE_LINE,"constant_coding","(Trivial): "+ "\""+expressionContent.get(i)+"\""+" uses explicit integer "+value,lineNumbers.get(i)));
                    break;
            }
        }


        // For remaining values, calculate the Hamming distance between them and warn user if below sensitivity value
        // Added exception
        //      * Only look inside switch statements for hamming comparison for now
        for (int i = 0; i < switchValues.size() - 1; i++) {
            for (int j = i + 1; j < switchValues.size(); j++) {
                int hamming = compareHamming(switchValues.get(i), switchValues.get(j));
                if (hamming <= sensitivity) {
                    output.appendResult(new ResultLine(ResultLine.MULTI_LOCATION,"constant_coding","(Low Hamming): Switch case lines "+switchLineNumbers.get(i)+" : "+ "\""+switchExpressionContent.get(i)+"\""+" and "+switchLineNumbers.get(j)+" : "+ "\""+switchExpressionContent.get(j)+"\""+" have a low Hamming distance ("+hamming+"). Consider more complex flags.",switchLineNumbers.get(i),switchLineNumbers.get(j)));
                }
            }
        }
    }
}
