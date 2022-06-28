package com.afivd.afivd;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
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

    /**
     * loadAndParseC prepares the generated C parser and parseTree with the C contents of the passed C file
     * @param filePath The file path of the C file
     * @return True if file load was successful, false otherwise
     */
    public boolean loadAndParseC(String filePath) {
        try {
            CharStream charStream = CharStreams.fromFileName(filePath); // 0. Load C File
            CLexer lexer = new CLexer(charStream);                      // 1. Get lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);    // 2. Get list of matched tokens
            this.parser = new CParser(tokens);                          // 3. Pass tokens to parser
            this.parseTree = this.parser.compilationUnit();             // 4. Generate ParseTree to scan through
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

        for (VariableSearcher.VariableTuple codeVariable : codeVariables) {
            System.out.println(codeVariable.getVariableType() + " : " + codeVariable.getVariableName());
        }

        // Crypto cryptoListener = new Crypto();
        ConstantCoding constantCodingListener = new ConstantCoding(results, 3);
        // Detect detectListener= new Detect();
        DefaultFail defaultFailListener = new DefaultFail(results);
        // Flow flowListener = new Flow();
        // DoubleCheck doubleCheckListener = new DoubleCheck();
        // LoopCheck loopCheckListener = new LoopCheck();
        Branch branchListener = new Branch(results);
        // Respond respondListener = new Respond();
        // Delay delayListener = new Delay();
        // Bypass bypassListener = new Bypass();


        // Now that all Fault Pattern objects have been created, use them in the ParseTreeWalker to have them 'listen'

        // TODO: Remove these extra function calls in the future so we can use loops to run each pattern

        // ParseTreeWalker.DEFAULT.walk(cryptoListener, parseTree);
        ParseTreeWalker.DEFAULT.walk(constantCodingListener, parseTree);
        constantCodingListener.analyze();
        ParseTreeWalker.DEFAULT.walk(branchListener, parseTree);
        // ParseTreeWalker.DEFAULT.walk(detectListener, parseTree);
        ParseTreeWalker.DEFAULT.walk(defaultFailListener, parseTree);
        defaultFailListener.runAtEnd();
        // ParseTreeWalker.DEFAULT.walk(doubleCheckListener, parseTree);
        // ParseTreeWalker.DEFAULT.walk(loopCheckListener, parseTree);
        // ParseTreeWalker.DEFAULT.walk(branchListener, parseTree);
        // ParseTreeWalker.DEFAULT.walk(respondListener, parseTree);
        // ParseTreeWalker.DEFAULT.walk(delayListener, parseTree);
        // ParseTreeWalker.DEFAULT.walk(bypassListener, parseTree);

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
