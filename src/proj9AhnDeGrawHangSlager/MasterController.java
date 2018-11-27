/*
 * File: MasterController.java
 * Names: Kevin Ahn, Lucas DeGraw, Jackie Hang, Kyle Slager
 * Class: CS 361
 * Project 7
 * Date: November 2, 2018
 * ---------------------------
 * Edited From: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 *
 */

package proj9AhnDeGrawHangSlager;


import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.event.Event;

import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Desktop;
import java.io.IOException;
import java.util.List;

import proj9AhnDeGrawHangSlager.bantam.util.CompilationException;
import proj9AhnDeGrawHangSlager.bantam.util.Error;

/**
 * This is the master controller for the program. it references
 * the other controllers for proper menu functionality.
 *
 /**
 * This class contains the handlers for each of the menu options in the IDE.
 *
 * Keeps track of the tab pane, the current tab, the index of the current tab
 * within the pane, and the File objects of the current tabs.
 *
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou (Project 5)
 * @author  Kevin Ahn, Lucas DeGraw, Jackie Hang, Kyle Slager
 * @version 4.0
 * @since   11-2-2018
 */
public class MasterController {
    @FXML private Menu editMenu;
    @FXML private JavaTabPane javaTabPane;
    @FXML private VBox vBox;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem closeMenuItem;
    @FXML private MenuItem darkModeMenuItem;
    @FXML private MenuItem normalModeMenuItem;
    @FXML private MenuItem funModeMenuItem;
    @FXML private MenuItem hallowThemeItem;
    @FXML private Console console;
    @FXML private Button stopButton;
    @FXML private Button compileButton;
    @FXML private Button compileRunButton;
    @FXML private TextField findTextEntry;
    @FXML private Button findPrevBtn;
    @FXML private Button findNextBtn;
    @FXML private TextField replaceTextEntry;
    @FXML private Menu prefMenu;
    @FXML private TreeView directoryTree;
    @FXML private Button scanButton;

    private EditController editController;
    private FileController fileController;
    private ToolbarController toolbarController;

    // this line from JianQuanMarcello project 6
    private ContextMenuController contextMenuController;



    @FXML
    public void initialize(){

        editController = new EditController(javaTabPane, findTextEntry, findPrevBtn, findNextBtn, replaceTextEntry);
        fileController = new FileController(vBox,javaTabPane);

        toolbarController = new ToolbarController(console,stopButton,compileButton,compileRunButton,javaTabPane);
        SimpleListProperty<Tab> listProperty = new SimpleListProperty<Tab> (javaTabPane.getTabs());
        editMenu.disableProperty().bind(listProperty.emptyProperty());
        saveMenuItem.disableProperty().bind(listProperty.emptyProperty());
        saveAsMenuItem.disableProperty().bind(listProperty.emptyProperty());
        closeMenuItem.disableProperty().bind(listProperty.emptyProperty());
        scanButton.disableProperty().bind(listProperty.emptyProperty());
        this.toolbarController.setReceivedCommand(false);
        this.console.setToolbarController(this.toolbarController);

        // this line from JianQuanMarcello project 6
        this.setupContextMenuController();

    }

    /**
     * Creates a reference to the ContextMenuController and passes in window items and other sub Controllers when necessary.
     * this method is from JianQuanMarcello project 6
     */
    private void setupContextMenuController() {
        this.contextMenuController = new ContextMenuController();
        this.contextMenuController.setFileMenuController(this.fileController);
        this.contextMenuController.setEditMenuController(this.editController);
        this.contextMenuController.setToolBarController(this.toolbarController);

        this.fileController.setContextMenuController(this.contextMenuController);
    }

    /**
     * Calls handleNewCommand() from the Toolbar Controller if the user
     * presses the enter key.
     * @param ke the key event
     */
    @FXML public void handleUserKeypress(KeyEvent ke){
        toolbarController.handleUserKeypress(ke);
    }

    /**
     * Calls toggleSingleComment from the Edit Controller
     *
     */
    @FXML public void handleCommenting() {
        editController.handleCommenting();
    }

    /**
     * Calls handleTabbing from the Edit Controller
     *
     */
    @FXML public void handleTabbing() {
        editController.handleTabbing();
    }

    /**
     * Calls handleUnTabbing from the Edit Controller
     *
     */
    @FXML public void handleUnTabbing() {
        editController.handleUnTabbing();
    }

    /**
     * Helper method that calls either Compile or Compile and Run
     * @param compileMethod a String the consists of either "handleCompile" or
     *                      "handleCompileAndRun"
     * @throws InterruptedException
     */
    private void callProperCompileMethod(String compileMethod) throws InterruptedException{
        if (compileMethod.equals("handleCompile")) {
            toolbarController.handleCompile(fileController.getFilePath());
        }
        else {
            toolbarController.handleCompileAndRun(fileController.getFilePath());
        }
    }

    /**
     * Helper method that checks if user wants to save a file before
     * compiling their code- It also calls the necessary compile method
     * @param compileMethod  a String the consists of either "handleCompile" or
     *                      "handleCompileAndRun"
     * @throws InterruptedException
     */
    private void compileHelper(String compileMethod) throws InterruptedException{
        toolbarController.disableCompileAndRunButtons();
        JavaTab curTab = (JavaTab)this.javaTabPane.getSelectionModel().getSelectedItem();
        if (!javaTabPane.getTabSavedStatus(curTab)) {

            String saveResult = toolbarController.handleCompileSaveDialog();
            if (saveResult == "yesButton") {
                fileController.handleSave();
                callProperCompileMethod(compileMethod);
            } else if (saveResult == "noButton") {
                if(fileController.getFilePath() == null){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Cannot compile a file with no previous saved version.");
                    alert.showAndWait();
                }
                callProperCompileMethod(compileMethod);
            }else{ return;}
        } else {
            callProperCompileMethod(compileMethod);
        }
    }

    /**
     * Handler for the Compile in the toolbar. Checks if the current file
     * has been saved. If it has not, prompts the user to save, if so,
     * compiles the program. If user chooses not to save, compiles last
     * version of the file.
     */
    @FXML public void handleCompile() throws InterruptedException{
        toolbarController.disableCompileAndRunButtons();
        compileHelper("handleCompile");
    }

    /**
     * Handler for the Compile and Run button in the toolbar.
     * Checks if the current file has been saved. If it has not,
     * prompts the user to save, if so, compiles and runs the program.
     * If user chooses not to save, compiles and runs the last
     * version of the file.
     */
    @FXML public void handleCompileAndRun() throws InterruptedException {
        toolbarController.disableCompileAndRunButtons();
        compileHelper("handleCompileAndRun");
    }

    /**
     * This method clears the console, tries to scan
     * and will write any errors to the console
     * @param event press of the Scan button triggering this method
     */
    @FXML public void handleScan(Event event) {

        this.console.clear();
        try {
            this.fileController.handleScan(event);
        } catch (CompilationException e) {
            this.console.writeLine(e.toString() + "\n", "ERROR");
            return;
        }

        List<Error> scanningErrors = fileController.getScanningErrors();

        if (scanningErrors != null) {

            for (Error e : scanningErrors)
                this.console.writeLine(e.toString() + "\n", "ERROR");

            this.console.writeLine(scanningErrors.size() +
                    " illegal tokens were found.", "ERROR");
        }
    }
    /**
     * Handler for the Stop button in the toolbar.
     * Calls the handleStop() method from Toolbar Controller and re-enables the toolbar buttons.
     */
    @FXML public void handleStop(){
        toolbarController.handleStop();
        if(this.javaTabPane.getTabs().isEmpty()) {
            this.stopButton.setDisable(true);
            return;
        }
        toolbarController.enableCompileAndRunButtons();
    }

    /**
     * Handles the find and replace button action.
     * Opens a find and replace popup window.
     */
    @FXML
    private void handleFindAndReplace() { editController.handleFindAndReplace(); }

    /**
     * Handler for the "About" menu item in the "File" menu.
     * Creates an Information alert dialog to display author and information of this program
     */
    @FXML public void handleAbout() {
        fileController.handleAbout();
    }

    /**
     * Handler for the "New" menu item in the "File" menu.
     * Adds a new Tab to the TabPane, and also adds null to the HashMap
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleNew() {
        fileController.handleNew( null ); // TODO: decide whether to create a new File object or not here
        if(!toolbarController.isTaskRunning() && this.javaTabPane.getTabs().size() > 0) {
            toolbarController.enableCompileAndRunButtons();
        }
    }

    /**
     * Handler for the "Open" menu item in the "File" menu.
     * Creates a FileChooser to select a file
     * Use scanner to read the file and write it into a new tab.
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleOpen() {
        fileController.handleOpen();
        if(!toolbarController.isTaskRunning() && this.javaTabPane.getTabs().size() > 0) {
            toolbarController.enableCompileAndRunButtons();
        }
    }

    /**
     * Handler for the "Close" menu item in the "File" menu.
     * Checks to see if the file has been changed since the last save.
     * If changes have been made, redirect to askSave and then close the tab.
     * Otherwise, just close the tab.
     */
    @FXML public void handleClose(Event event) {
        fileController.handleClose(event);
//        if (this.tabPane.getTabs().isEmpty()&&!toolbarController.isTaskRunning()){
//            disableToolbar();
//        }

    }

    /**
     * Handler for the "Save" menu item in the "File" menu.
     * If the current tab has been saved before, writes out the content to its corresponding
     * file in storage.
     * Else if the file has never been saved, opens a pop-up window that allows the user to
     * choose a filename and directory and then store the content of the tab to storage.
     */
    @FXML public void handleSave() {
        fileController.handleSave();
    }

    /**
     * Handler for the "Save as..." menu item in the "File" menu.
     * Opens a pop-up window that allows the user to choose a filename and directory.
     * Calls writeFile to save the file to memory.
     * Changes the name of the current tab to match the newly saved file's name.
     */
    @FXML public void handleSaveAs( ) {
        fileController.handleSaveAs();
    }

    /**
     * Handler for the "Exit" menu item in the "File" menu.
     * Closes all the tabs using handleClose()
     * Returns when the user cancels exiting any tab.
     */
    @FXML public void handleExit(Event event) {
        toolbarController.handleStop();
        fileController.handleExit(event);
    }

    /**
     * Handler for the "Undo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleUndo() { editController.handleUndo(); }

    /**
     * Handler for the "Redo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleRedo() {
        editController.handleRedo(); }

    /**
     * Handler for the "Cut" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCut() {
        editController.handleCut(); }

    /**
     * Handler for the "Copy" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCopy() {
        editController.handleCopy();}

    /**
     * Handler for the "Paste" menu item in the "Edit" menu.
     */
    @FXML
    public void handlePaste() {
        editController.handlePaste(); }

    /**
     * Handler for the "SelectAll" menu item in the "Edit" menu.
     */
    @FXML
    public void handleSelectAll() {
        editController.handleSelectAll(); }

    /**
     * Changes the theme of the IDE to Dark
     */
    @FXML
    public void handleDarkMode(){
       handleThemeChange("proj9AhnDeGrawHangSlager/resources/DarkMode.css", darkModeMenuItem);
    }

    /**
     * Changes the theme of the IDE back to normal
     */
    @FXML
    public void handleNormalMode(){
        vBox.getStylesheets().remove(vBox.getStylesheets().size()-1);
        enableUnselectedThemes(normalModeMenuItem);

    }

    /**
     * Changes the theme of the IDE to Fun Mode
     */
    @FXML
    public void handleFunMode(){
        handleThemeChange("proj9AhnDeGrawHangSlager/resources/FunMode.css", funModeMenuItem);

    }


    /**
     * Changes the theme of the IDE to HallowTheme--
     * a fun Halloween extra!
     */
    @FXML
    public void handleHallowThemeMode(){
        handleThemeChange("proj9AhnDeGrawHangSlager/resources/HallowTheme.css", hallowThemeItem);
    }
    /**
     * Helper method to change the theme
     * @param themeCSS
     */
    private void handleThemeChange(String themeCSS, MenuItem menuItem){
        if(vBox.getStylesheets().size() > 1){
            vBox.getStylesheets().remove(vBox.getStylesheets().size()-1);
        }
        vBox.getStylesheets().add(themeCSS);
        enableUnselectedThemes(menuItem);
    }

    /**
     * Enables the menu items of themes that aren't currently used and
     * disables the menu item of the theme that is currently on
     * display
     *
     * @param menuItem the menu item that needs to be disabled
     */
    private void enableUnselectedThemes(MenuItem menuItem){
        for(MenuItem item: prefMenu.getItems()){
            if(!item.equals(menuItem)){
                item.setDisable(false);
            }
            else{
                item.setDisable(true);
            }
        }
    }

    /**
     * Code for handleOpenJavaTutorial obtained from Li-Lian-KeithHardy-Zhou
     * Handler for the "Java Tutorial" menu item in the "Help" Menu.
     * When the item is clicked, a Java tutorial will be opened in a browser.
     */
    @FXML
    public void handleOpenJavaTutorial(){
        try {
            URI url = new URI("https://docs.oracle.com/javase/tutorial/");
            Desktop.getDesktop().browse(url);
        } catch (IOException|URISyntaxException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * Disables the Compile, Compile and Run, and Stop buttons in the toolbar
//     */
//    private void disableToolbar(){
//        this.compileButton.setDisable(true);
//        this.compileRunButton.setDisable(true);
//        this.stopButton.setDisable(true);
//    }

    /**
     * Calls handleMatchBracketOrParen() of the editController
     */
    @FXML
    public void handleMatchBracketOrParen() {
        editController.handleMatchBracketOrParen();
    }

    /**
     * Calls handleFindText() of the editController
     */
    @FXML
    public void handleFindText() {
        editController.handleFindText(true);
    }

    /**
     * Calls handleHighlightPrevMatch() of the editController
     */
    @FXML
    public void handleHighlightPrevMatch() {
        editController.handleHighlightPrevMatch();
    }

    /**
     * Calls handleHighlightNextMatch() of the editController
     */
    @FXML
    public void handleHighlightNextMatch() {
        editController.handleHighlightNextMatch();
    }

    /**
     * Calls handleReplaceText() of the editController
     */
    @FXML
    public void handleReplaceText() {editController.handleReplaceText(); }

    /**
     * Focuses on the Find Text Entry Box
     */
    @FXML
    public void handleFocusOnFindTextEntry() {
        this.findTextEntry.requestFocus();
    }

    /**
     * Focuses on the Replace Text Extry Box
     */
    @FXML
    public void handleFocusOnReplaceTextEntry() {
        this.replaceTextEntry.requestFocus();
    }



}