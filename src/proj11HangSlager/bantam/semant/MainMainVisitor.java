/*
 * File: MainMainVisitor.java
 * Names: Jackie Hang, Kyle Slager
 * Class: CS361
 * Project 11
 * Date: February 13, 2019
 */



package proj11HangSlager.bantam.semant;
import proj11HangSlager.bantam.ast.*;
import proj11HangSlager.bantam.visitor.Visitor;


/**
 * This Vistor class uses the Visitor pattern to visit all the
 * nodes of a program AST, and checks to see if there
 * is a Main class as well as a Main method
 *
 *
 * @author  Jackie Hang, Kyle Slager
 * @version 1.0
 * @since   2-13-19
 */

public class MainMainVisitor extends Visitor{

    boolean hasBeenFound = false;


    /**
     * Checks to see if both a Main method and Main
     * class exists
     * @param ast
     * @return boolean of existance
     */
    public boolean hasMain(Program ast) {
        ast.accept(this);
        return hasBeenFound;
    }

    /**
     * Overrides the Visit method that takes in a Class node
     * Sees if there is a main class
     * @param node the class node
     * @return
     */
    public Object visit(Class_ node){
        super.visit(node);
        if(node.getName().equals("Main")){
            node.getMemberList().accept(this);
        }

        return null;
    }

    /**
     * Overrides the Visit method that takes in a method node
     * Sees if there is a main method
     * @param node the method node
     * @return
     */
    public Object visit(Method node) {
        super.visit(node);
        if((node.getName().equals("main")) && (node.getFormalList().getSize() == 0) && (node.getReturnType().equals("void"))) {
            hasBeenFound = true;
        }
        return null;
    }
}