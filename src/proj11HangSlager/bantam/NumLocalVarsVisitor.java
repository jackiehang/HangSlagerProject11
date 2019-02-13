package proj11HangSlager.bantam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import proj11HangSlager.bantam.ast.*;
import proj11HangSlager.bantam.visitor.Visitor;



public class NumLocalVarsVisitor extends Visitor{

    private ArrayList<Integer> numVars = new ArrayList<>();
    private ArrayList<String> methodNames = new ArrayList<>();
    private int numCurVars = 0;

    public Map<String,Integer> getNumLocalVars(Program ast){
        Map<String,Integer> numVarsMap = new HashMap<String,Integer>();
        ast.accept(this);
        for(int i = 0; i < numVars.size(); i++){
            numVarsMap.put(methodNames.get(i).substring(4), numVars.get(i));
        }
        return numVarsMap;
    }

    public Object visit(Class_ node){
        super.visit(node);
        for(int i = 0; i < methodNames.size(); i++){
            if(!methodNames.get(i).contains("%%%%")) {
                methodNames.set(i, "%%%%" + node.getName() + "." + methodNames.get(i));
            }
        }
        return null;
    }

    public Object visit(Method node){
        super.visit(node);
        methodNames.add(node.getName());
        numVars.add(numCurVars);
        numCurVars = 0;
        return null;
    }

    public Object visit(DeclStmt node){
        numCurVars++;
        return null;

    }




}



