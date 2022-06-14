// ResultLine.java
// Started 6_7_22 during RHEST Summer 2022
// Logan Reichling and Ikran Warsame
package com.afivd.afivd;

/**
 * ResultLine stores each line of results with the relevant code line it correlates with
 */
public class ResultLine{
    // Flags
    public static final int GENERAL_COMMENT = 0b00000000; // Results/Advice that do not pertain to any specific line
    public static final int SINGLE_LINE     = 0b00000001; // Results/Advice that pertain to a single line
    public static final int MULTI_LOCATION  = 0b00000010; // Results/Advice that pertain to multiple, potentially noncontinuous lines
    public static final int SPANNING_RESULT = 0b00000011; // Results/Advice that pertain to a continuous range of code lines

    // Private Variables
    private final int[] lineNumbers;
    private final String resultContents;
    private int mode;

    // Getters
    // TODO: Use getters to enable highlighting of the loaded code file
    public int[] getLineNumbers() {return lineNumbers;}
    public String getResultContents() {return resultContents;}
    public int getMode() {return mode;}

    /**
     * ResultLine constructor requires mode, advice or other result contents, and a variables list of line numbers
     * @param mode Determines how the line numbers and result are handled, see flag declarations at start of class
     * @param resultContents Advice/Results to be stored
     * @param lineNumbers The line numbers which are pertinent to the result, see flag declarations at start of class
     */
    public ResultLine(int mode, String resultContents, int... lineNumbers){
        this.mode = mode;
        if(lineNumbers.length == 0){
            this.mode = GENERAL_COMMENT; // Quick check in case varargs are not passed
        }
        this.lineNumbers = lineNumbers;
        this.resultContents = resultContents;
    }

    @Override
    public String toString() {
        switch(this.mode) {
            // TODO: Make comments available in a different output, rather than also allow them to be returned
            case GENERAL_COMMENT:
                return "Comment: "+resultContents;
            case SINGLE_LINE:
                return "Line: "+lineNumbers[0]+" | "+resultContents;
            case MULTI_LOCATION:
                StringBuilder locationList = new StringBuilder();
                for(int location : lineNumbers){
                    locationList.append(location).append(", ");
                }
                locationList.setLength(locationList.length()-2);
                return "Lines: "+ locationList +" | "+resultContents;
            case SPANNING_RESULT:
                return "Line: "+lineNumbers[0]+" to "+lineNumbers[1]+" | "+resultContents;
        }
        return null;
    }
}
