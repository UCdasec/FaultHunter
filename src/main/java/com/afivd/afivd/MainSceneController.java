package com.afivd.afivd;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

public class MainSceneController {
    String cFilePath;

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

    // ----------------------------------- Event Handlers -----------------------------------
    /**
     * LoadFileButton corresponds to the 'Load File' button that opens up a File Chooser window
     */
    @FXML
    protected void loadFileButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a C file...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("C Files (*.c)","*.c"));
        File cFile = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());
        try {
            if (cFile.isFile() && cFile.canRead() && cFile.canWrite()) {
                codeTextArea.setText(new String(Files.readAllBytes(Paths.get(cFile.toURI()))));
                commentTextArea.clear();
                this.cFilePath = cFile.getAbsolutePath();
                loadFileButton.setDisable(true);
                runButton.setDisable(false);
            }
        }catch (NullPointerException ignored){ // When user closes the open dialog
        }catch (Exception e){
            System.out.println("Err: Can't access file");
            e.printStackTrace();
        }
    }

    @FXML
    protected void runButton(){
        Analyze analyze = new Analyze();
        if(analyze.loadAndParseC(this.cFilePath)){
            //int counter = 0;
            ArrayList<String> results = analyze.runTests();
            for (String result : results) {
                //counter++;
                commentTextArea.appendText(result);
            }
            // TODO: Fix count to something later
            // commentLabel.setText("Comments: ("+counter+" Patterns Used )");

        }else{
            System.out.println("Err: C File not parsed");
        }
        if (showTreeCheckbox.isSelected()){
            analyze.showDebugTree();
        }
        analyze.clearParser();
        analyze = null;
        runButton.setDisable(true);
        loadFileButton.setDisable(false);
    }
}