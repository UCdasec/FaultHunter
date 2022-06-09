package com.afivd.afivd;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Future;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import javax.swing.*;

public class Analyze {

    // Default Constructor

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


    public ArrayList<String> runTests(){
        // TODO: Have this function return a arraylist of strings of outputs of those functions
        // Use our created listener classes and walk the tree

        // Probably just going to pass this to the constructors of the Listener classes and use some ID to get the specific results,
        // Most likely there is a better way to do this, but it will work
        ArrayList<String> results = new ArrayList<>();

        // Zero is start of index
        // Crypto cryptoListener = new Crypto();
        ConstantCoding constantCodingListener = new ConstantCoding(parser, 3, results);
        // Detect detectListener= new Detect();
        // DefaultFail defaultFailListener = new DefaultFail();
        // Flow flowListener = new Flow();
        // Doublecheck doublecheckListener = new Doublecheck();
        // Loopcheck loopcheckListener = new Loopcheck();
        Branch branchListener = new Branch(parser, results);
        // Respond respondListener = new Respond();
        // Delay delayListener = new Delay();
        // Bypass bypassListener = new Bypass();


        // ParseTreeWalker.DEFAULT.walk(cryptoListener, compilationUnitContext);
        results.add("Detecting constant coding vulnerability...\n------------------------------------------");
        ParseTreeWalker.DEFAULT.walk(constantCodingListener, parseTree);
        constantCodingListener.analyze();
        results.add("\nDetecting branch vulnerability...\n------------------------------------------\n");
        ParseTreeWalker.DEFAULT.walk(branchListener, parseTree);
        // ParseTreeWalker.DEFAULT.walk(detectListener, compilationUnitContext);
        // ParseTreeWalker.DEFAULT.walk(defaultFailListener, compilationUnitContext);
        // ParseTreeWalker.DEFAULT.walk(doublecheckListener, compilationUnitContext);
        // ParseTreeWalker.DEFAULT.walk(loopcheckListener, compilationUnitContext);
        // ParseTreeWalker.DEFAULT.walk(branchListener, compilationUnitContext);
        // ParseTreeWalker.DEFAULT.walk(respondListener, compilationUnitContext);
        // ParseTreeWalker.DEFAULT.walk(delayListener, compilationUnitContext);
        // ParseTreeWalker.DEFAULT.walk(bypassListener, compilationUnitContext);



        return results;
    }

    public void showDebugTree(){
        // Return results to display
        Future<JFrame> treeWindow = Trees.inspect(parseTree, parser);
    }

    public void clearParser(){
        this.parser = null;
        this.parseTree = null;
    }


}
