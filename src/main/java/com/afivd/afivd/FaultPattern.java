package com.afivd.afivd;

/**
 * FaultPattern Interface used to simplify running all the FaultPatterns and their additional methods (if necessary)
 */
public interface FaultPattern {
    /**
     * Method that runs after running the parse tree
     */
    void runAtEnd();
}
