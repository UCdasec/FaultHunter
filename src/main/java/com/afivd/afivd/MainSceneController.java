// MainSceneController.java
// Started 6_7_22 during RHEST Summer 2022
// Logan Reichling and Ikran Warsame
package com.afivd.afivd;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;
import javafx.scene.web.WebView;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * MainSceneController handles interaction with JavaFX UI elements
 */
public class MainSceneController {
    private String cFilePath;
    private int numCodeLines;
    private ArrayList<String> codeLines;

    // ----------------------------------- UI Variables -----------------------------------
    @FXML
    private TextArea codeTextArea;
    @FXML
    private Button loadFileButton;
    @FXML
    private Button runButton;
    @FXML
    private Label commentLabel;
    @FXML
    private TextArea commentTextArea;
    @FXML
    private CheckBox showTreeCheckbox;
    @FXML
    private ToggleGroup showToggleGroup;
    @FXML
    private RadioButton showCommentsToggleButton;
    @FXML
    private RadioButton showReplacementsToggleButton;
    @FXML
    private TextArea replacementTextArea;
    @FXML
    private Label currentDocumentLabel;
    @FXML
    private Label executionTimeLabel;

    //@FXML
    //private WebView codeWebView;

    // ----------------------------------- Button Handlers -----------------------------------
    /**
     * LoadFileButton corresponds to the 'Load File' button that opens up a File Chooser window
     * Displays loaded code file, but does not run scans yet
     */
    @FXML
    protected void loadFileButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a C file...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("C Files (*.c)","*.c"));
        File cFile = fileChooser.showOpenDialog(this.loadFileButton.getScene().getWindow());
        try {
            if (cFile.isFile() && cFile.canRead() && cFile.canWrite()) {
                this.codeLines = new ArrayList<>(Files.readAllLines(Paths.get(cFile.toURI())));
                this.numCodeLines = this.codeLines.size();
                this.codeTextArea.setText("");
                StringBuilder plainCode = new StringBuilder();
                for(int i = 0; i<this.numCodeLines;i++){
                    plainCode.append(i+1).append(": ").append(this.codeLines.get(i)).append("\n"); // Line number appended
                }
                this.codeTextArea.setText(plainCode.toString());
                this.currentDocumentLabel.setText("Current Document: "+cFile.getName());
                this.commentTextArea.clear();
                this.replacementTextArea.clear();
                this.cFilePath = cFile.getAbsolutePath();

                // Enable run button
                this.loadFileButton.setDisable(true);
                this.runButton.setDisable(false);
            }
        }catch (NullPointerException ignored){ // When user closes the open dialog
        }catch (Exception e){
            System.out.println("Err: Can't access file");
            e.printStackTrace();
        }
    }

    /**
     * When the runButton is pressed, every fault pattern is run and the results are retrieved using the Analyze class.
     */
    @FXML
    protected void runButton(){
        // Give codeLines for patterns that offer replacement

        // Start timer:
        long startTime = System.nanoTime();

        Analyze analyze = new Analyze(this.codeLines);
        if(analyze.loadAndParseC(this.cFilePath)){
            List<ResultLine> results = analyze.runFaultPatterns().getResults();

            if(results.size()!=0){
                // Sort before displaying
                Collections.sort(results, new Comparator<ResultLine>() {
                    @Override
                    public int compare(ResultLine lhs, ResultLine rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, ...inverse if you want opposite direction
                        return Integer.compare(lhs.getLineNumbers()[0], rhs.getLineNumbers()[0]);
                    }
                });

                for (ResultLine result : results) {
                    this.commentTextArea.appendText(result.toString()+"\n");
                }
                StringBuilder replacementCode = new StringBuilder();
                for(int i = 0; i<this.codeLines.size();i++){
                    replacementCode.append(this.codeLines.get(i)).append("\n"); // Line number appended
                }
                this.replacementTextArea.setText(replacementCode.toString());
            }else{
                this.commentTextArea.setText("No suggestions!");
                this.replacementTextArea.setText("No replacements!");
            }
        }else{System.out.println("Err: C File not parsed");}

        // Stop timer for vulnerability analysis
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;  // in milliseconds

        executionTimeLabel.setText(duration +" ms");

        // If 'Show Tree' is selected
        if (this.showTreeCheckbox.isSelected()){
            analyze.showDebugTree();
        }

        // Clean-up
        analyze.clearParser();
        analyze = null;
        clearStaleData();

        // Set buttons
        this.runButton.setDisable(true);
        this.loadFileButton.setDisable(false);
    }

    /**
     * When 'Show Comments' is selected, only the commentsTextArea is shown, while when 'Show Replacements' is selected,
     * only the replacementsTextArea is shown.
     */
    @FXML
    protected void onShowToggleSelection(){
        if(this.showCommentsToggleButton.isSelected()){
            this.commentLabel.setText("Comments: ");
            this.commentTextArea.setVisible(true);
            this.replacementTextArea.setVisible(false);

        }else if(this.showReplacementsToggleButton.isSelected()){
            this.commentLabel.setText("Replacements: ");
            this.commentTextArea.setVisible(false);
            this.replacementTextArea.setVisible(true);
        }
    }


    /*
    private void runWebView(ParsedResults results){
        WebEngine webEngine = this.codeWebView.getEngine();
        webEngine.loadContent("","text/html"); // Set to HTML file created by ResultsToWebView

    }

     */

    /**
     * Helper function used to help clear stale data after running the application and ensure GC does its job.
     */
    private void clearStaleData(){
        this.cFilePath = null;
        this.numCodeLines = 0;
        this.codeLines = null;
    }
}