/*
 * File: NumLocalVarsVisitor.java
 * Names: Jackie Hang, Kyle Slager
 * Class: CS361
 * Project 11
 * Date: February 13, 2019
 */

package proj11HangSlager.bantam.semant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import proj11HangSlager.bantam.ast.*;
import proj11HangSlager.bantam.visitor.Visitor;

/**
 * This Visitor class uses the Visitor pattern to
 * visit every node of a program AST, but specifically captures
 * the number of local variables in every method.
 *
 *
 * @author  Jackie Hang, Kyle Slager
 * @version 1.0
 * @since   2-13-19
 */
public class NumLocalVarsVisitor extends Visitor{

    private ArrayList<Integer> numVars = new ArrayList<>();
    private ArrayList<String> methodNames = new ArrayList<>();
    private int numCurVars = 0;

    /**
     * Creates a map of every method and the number of
     * local variables in them.
     *
     * @param ast
     * @return Map<String, Integer>
     */
    public Map<String,Integer> getNumLocalVars(Program ast){
        Map<String,Integer> numVarsMap = new HashMap<String,Integer>();
        ast.accept(this);
        for(int i = 0; i < numVars.size(); i++){
            numVarsMap.put(methodNames.get(i).substring(4), numVars.get(i));
        }
        return numVarsMap;
    }

    /**
     * Overrides the visit method of the visitor
     * that takes in a Class node.
     *
     * Ensures that all method names have a single class
     * attached to it.
     *
     * @param node the class node
     * @return
     */
    public Object visit(Class_ node){
        super.visit(node);
        for(int i = 0; i < methodNames.size(); i++){
            if(!methodNames.get(i).contains("%%%%")) {
                methodNames.set(i, "%%%%" + node.getName() + "." + methodNames.get(i));
            }
        }
        return null;
    }

    /**
     * Overrides the visit method of the visitor
     * that takes in a Method node.
     *
     * Adds all the method names to a map.
     *
     * @param node the method node
     * @return
     */
    public Object visit(Method node){
        super.visit(node);
        methodNames.add(node.getName());
        numVars.add(numCurVars);
        numCurVars = 0;
        return null;
    }

    /**
     * Overrides the visit method of the visitor
     * that takes in a Method node.
     *
     * Increments the counter for every local var found
     * in a method
     * @param node the declaration statement node
     * @return
     */
    public Object visit(DeclStmt node){
        numCurVars++;
        return null;

    }




}



