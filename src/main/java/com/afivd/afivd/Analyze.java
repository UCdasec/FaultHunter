package com.afivd.afivd;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * The Analyze class acts as a container to run all of our created Fault patterns and also handle initially parsing
 * the passed C file using ANTLR.
 */
public class Analyze {
    private CParser parser;
    private CParser.CompilationUnitContext parseTree;
    private final ArrayList<String> codeLines;

    public Analyze(ArrayList<String> codeLines){
        this.codeLines = codeLines;
    }

    /**
     * loadAndParseC prepares the generated C parser and parseTree with the C contents of the passed C file
     * @param filePath The file path of the C file
     * @return True if file load was successful, false otherwise
     */
    public boolean loadAndParseC(String filePath) {
        try {
            CharStream charStream    = CharStreams.fromFileName(filePath);      // 0. Load C File
            CLexer lexer             = new CLexer(charStream);                  // 1. Get lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);            // 2. Get list of matched tokens
            this.parser              = new CParser(tokens);                     // 3. Pass tokens to parser
            this.parseTree           = this.parser.compilationUnit();           // 4. Generate ParseTree to scan through
            return true;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * RunFaultPatterns runs each pattern created to generate a set of result for the code file including advice
     * @return A ParsedResults storage object that contains all the results from the fault pattern analysis
     */
    public ParsedResults runFaultPatterns(){

        // Output ParsedResults object that will be passed to each Listener to fill with their results
        ParsedResults results = new ParsedResults();

        // VariableSearcher will be used to collect variables from the code file for use by specific patterns
        VariableSearcher variableSearcher = new VariableSearcher();
        ParseTreeWalker.DEFAULT.walk(variableSearcher,parseTree);
        ArrayList<VariableSearcher.VariableTuple> codeVariables = variableSearcher.getVariables();

        // Hold all FaultPatterns to be run in an array to run in a loop
        ArrayList<FaultPattern> faultPatterns = new ArrayList<>();

        // The code stored in codeLines can be modified without worry that it will affect other patterns, parseTree is
        // already created. Make sure to order Fault Patterns appropriately if the codeLines will be modified

        // Crypto cryptoListener = new Crypto();
        faultPatterns.add(new ConstantCoding(results, 3));
        // Detect detectListener= new Detect();
        faultPatterns.add(new DefaultFail(results));
        // Flow flowListener = new Flow();
        // DoubleCheck doubleCheckListener = new DoubleCheck();
        faultPatterns.add(new LoopCheck(results,codeVariables,codeLines));
        faultPatterns.add(new Branch(results));
        // Respond respondListener = new Respond();
        // Delay delayListener = new Delay();
        // Bypass bypassListener = new Bypass();

        // !!! codeLines used by GUI at end of analyze to display code with added replacements.


        // Now that all Fault Pattern objects have been created, use them in the ParseTreeWalker to have them 'listen'
        // Additionally, run all closing function (which does nothing by default)
        for(FaultPattern faultPattern : faultPatterns){
            ParseTreeWalker.DEFAULT.walk((ParseTreeListener) faultPattern,parseTree);
            faultPattern.runAtEnd();
        }

        return results;
    }

    /**
     * Pops up a Swing JFrame window when called with the created c parse tree
     */
    public void showDebugTree(){
        // Return results to display using Swing
        Future<JFrame> treeWindow = Trees.inspect(parseTree, parser);
        // Make sure window doesn't appear off the screen
        try {treeWindow.get().setLocation(0,0);} catch (Exception ignored) {}
    }

    /**
     * Removes references to the used objects to ensure that they are garbage collected
     */
    public void clearParser(){
        this.parser = null;
        this.parseTree = null;
    }
}