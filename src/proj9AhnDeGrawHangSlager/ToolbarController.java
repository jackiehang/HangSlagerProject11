/*
 * File: ToolbarController.java
 * Names: Kevin Ahn, Lucas DeGraw, Jackie Hang, Kyle Slager
 * Class: CS 361
 * Project 7
 * Date: November 2, 2018
 * ---------------------------
 * Edited From: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 */

package proj9AhnDeGrawHangSlager;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.io.*;

import java.util.Optional;
import java.util.concurrent.*;



/**
 * This class is the controller for all of the toolbar functionality.
 * Specifically, the compile, compile and run, and stop buttons
 *
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou (Project 5)
 * @version 2.0
 * @since   11-2-2018
 *
 */
public class ToolbarController {

    private FutureTask<Boolean> curFutureTask;
    private Console console;
    private Button stopButton;
    private Button compileButton;
    private Button compileRunButton;
    private TabPane tabPane;
    private boolean receivedCommand = false;

    ToolbarController(Console console, Button stopButton, Button compileButton, Button compileRunButton, TabPane tabPane){
        this.console = console;
        this.tabPane = tabPane;
        this.stopButton = stopButton;
        this.compileButton = compileButton;
        this.compileRunButton = compileRunButton;
    }

    /**
     *  Compiles the code currently open, assuming it has been saved.
     * @param filename the name of the file to compile
     */
    public void handleCompile(String filename){
        Thread compileThread = new Thread(()->compileFile(filename));
        compileThread.start();
    }

    /**
     * Calls compile and runs the code
     * @param filename the name of the file to compile and run
     */
    public void handleCompileAndRun(String filename){
        Thread compileRunThread = new Thread(() -> compileRunFile(filename));
        compileRunThread.start();
    }

    /**
     * Stops all currently compiling files and any currently running Java programs
     */
    public void handleStop(){
        if(curFutureTask!=null) {
            this.curFutureTask.cancel(true);
            this.console.writeLine("Process terminated.\n", "CONS");
        }
    }

    /**
     * Tells the Console that a user-input command was given
     * @param ke Reads in the key pressed
     */
    public void handleUserKeypress(KeyEvent ke){
        // check if a program is running
        if (this.curFutureTask == null||this.curFutureTask.isDone()){
            return;
        }
        // if enter key was pressed
        if(ke.getCode() == KeyCode.ENTER){
            receivedCommand = true;
        }
    }

    /**
     * Called when trying to compile something that was unsaved
     * @return the text corresponding with which button the user chose
     */
    public String handleCompileSaveDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Do you want to save your changes?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == yesButton){
            return "yesButton";
        }
        else if(result.get() == noButton){
            return "noButton";
        }
        else{
            return "cancelButton";
        }

    }

    /**
     * Compiles the specified file using the javac command
     * @param filename the name of the file to compile
     * @return whether or not compilation was successful
     */
    private boolean compileFile(String filename) {

        // create and run the compile process
        ProcessBuilder pb = new ProcessBuilder("javac", filename);
        CompileOrRunTask compileTask = new CompileOrRunTask(this.console, pb);
        this.curFutureTask = new FutureTask<Boolean>(compileTask);
        ExecutorService compileExecutor = Executors.newFixedThreadPool(1);
        compileExecutor.execute(curFutureTask);

        // Check if compile was successful, and if so, indicate this in the console
        Boolean compSuccessful = false;
        try {
            compSuccessful = curFutureTask.get();
            if (compSuccessful) {
                Platform.runLater(() ->
                        this.console.writeLine("Compilation was Successful.\n", "CONS"));
            }
            compileExecutor.shutdown();
        } catch (ExecutionException | InterruptedException | CancellationException e) {
            compileTask.stop();
        }

        if (this.isTaskRunning()) disableCompileAndRunButtons();
        else enableCompileAndRunButtons();

        return compSuccessful;
    }

    /**
     * Compiles and runs the specified file using the java command
     * @param fileNameWithPath the file name, including its path
     */
    private void compileRunFile(String fileNameWithPath){

        // Try to compile
        boolean compSuccessful = compileFile(fileNameWithPath);
        if(!compSuccessful){
            enableCompileAndRunButtons();
            return;
        }
        // Disable appropriate compile buttons
        disableCompileAndRunButtons();

        // set up the necessary file path elements
        int pathLength = fileNameWithPath.length();
        File file = new File(fileNameWithPath);
        String filename = file.getName();
        String filepath = fileNameWithPath.substring(0,pathLength-filename.length());
        int nameLength = filename.length();
        String classFilename = filename.substring(0, nameLength - 5);

        // Run the java program
        ProcessBuilder pb = new ProcessBuilder("java","-cp",filepath ,classFilename);
        CompileOrRunTask runTask = new CompileOrRunTask(console,pb);
        this.curFutureTask = new FutureTask<Boolean>(runTask);
        ExecutorService curExecutor = Executors.newFixedThreadPool(1);
        curExecutor.execute(this.curFutureTask);

        try{
            curExecutor.shutdown();
        }
        // if the program is interrupted, stop running
        catch (CancellationException e){
            runTask.stop();
            enableCompileAndRunButtons();
        }

        if (this.tabPane.getTabs().isEmpty()){
            this.stopButton.setDisable(true);
        }

        if (this.isTaskRunning()) disableCompileAndRunButtons();
        else enableCompileAndRunButtons();
    }


    /**
     * An inner class used for a thread to execute the run task
     * Designed to be used for compilation or running.
     * Writes the input/output error to the console.
     */
    private class CompileOrRunTask implements Callable{
        private Process curProcess;
        private Console console;
        private ProcessBuilder pb;

        /**
         * Initializes this compile/run task
         * @param console where to write output to
         * @param pb the ProcessBuilder we have used to call javac/java
         */
        CompileOrRunTask(Console console, ProcessBuilder pb){
            this.console = console;
            this.pb = pb;
        }

        /**
         * Starts the process
         * @return will return false if there is an error, true otherwise.
         * @throws IOException error reading input/output to/from console
         */
        @Override
        public Boolean call() throws IOException{
            this.curProcess = pb.start();
            BufferedReader stdInput, stdError;
            BufferedWriter stdOutput;
            stdInput = new BufferedReader(new InputStreamReader(this.curProcess.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(this.curProcess.getErrorStream()));
            stdOutput = new BufferedWriter((new OutputStreamWriter(this.curProcess.getOutputStream())));

            // Input to the console from the program
            String inputLine;

            // Errors from the executing task
            String errorLine = null;

            // True if there are no errors
            Boolean taskSuccessful = true;

            // A separate thread that checks for user input to the console
            new Thread(()->{
                while(this.curProcess.isAlive()){
                    if(receivedCommand){
                        try {
                            stdOutput.write(this.console.getConsoleCommand());
                            receivedCommand = false;
                            stdOutput.flush();
                        }catch (IOException e){this.stop();}
                    }
                }
            }).start();


            int inp;
            int err = -1;
            // While there is some input to the console, or errors that have occurred,
            // append them to the console for the user to see.
            while ((inp = stdInput.read()) >= 0 || (err = stdError.read()) >= 0){

                final char finalInput = (char)inp;
                final char finalError = (char)err;

                if (inp >= 0) {
                    Platform.runLater(() -> this.console.writeLine(Character.toString(finalInput), "INPUT"));
                }
                if(err >= 0) {
                    taskSuccessful = false;
                    Platform.runLater(() -> this.console.writeLine(Character.toString(finalError), "ERROR"));
                }
                try {
                    Thread.sleep(2);
                }catch (InterruptedException e){
                    this.stop();
                    return taskSuccessful;
                }
            }
            stdError.close();
            stdInput.close();
            stdOutput.close();
//            this.curProcess.get();
            return taskSuccessful;
        }

        /**
         * Stop the current process
         */
        public void stop(){
            if(this.curProcess != null){
                curProcess.destroyForcibly();
            }
        }
    }

    /**
     * Check if the task is still running.
     * @return true if this task is running, and false otherwise
     */
    public boolean isTaskRunning(){
        if(this.curFutureTask == null){
            return false;
        }
        else{
            try {
                if (this.curFutureTask.get()) return !this.curFutureTask.isDone();
            }
            catch(Exception e) {
                this.curFutureTask.cancel(true);
            }
            return false;
        }
    }

    /**
     * Disables the Compile and Compile and Run buttons, enables the Stop button.
     */
    public void disableCompileAndRunButtons() {
//        this.compileButton.setDisable(true);
//        this.compileRunButton.setDisable(true);
//        this.stopButton.setDisable(false);
    }

    /**
     * Enables the Compile and Compile and Run buttons, disables the Stop button.
     */
    public void enableCompileAndRunButtons() {
//        this.compileButton.setDisable(false);
//        this.compileRunButton.setDisable(false);
//        this.stopButton.setDisable(true);
    }

    public void setReceivedCommand(Boolean ifReceived){
        this.receivedCommand = ifReceived;
    }

}