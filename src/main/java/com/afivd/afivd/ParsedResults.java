// ParsedResults.java
// Started 6_14_22 during RHEST Summer 2022
// Logan Reichling and Ikran Warsame
package com.afivd.afivd;

import java.util.ArrayList;

/**
 * ParsedResults is used to hold the results of a Fault Pattern command being run, for use later
 */
public class ParsedResults {
    private final ArrayList<ResultLine> internalResults;

    public ParsedResults(){
        this.internalResults = new ArrayList<>();

    }
    public ParsedResults(ResultLine resultLine){
        this.internalResults = new ArrayList<>();
        this.internalResults.add(resultLine);
    }

    /**
     * Appends a result to the currently stored results.
     * @param result Filled ResultLine object from Fault Pattern class
     */
    public void appendResult(ResultLine result){
        this.internalResults.add(result);
    }

    /**
     * Returns the StringBuilder object with the current results
     * @return StringBuilder object with current results (check for null)
     */
    public ArrayList<ResultLine> getResults(){
        return this.internalResults;
    }

}