package proj11HangSlager.bantam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import proj11HangSlager.bantam.ast.*;
import proj11HangSlager.bantam.visitor.Visitor;



public class NumLocalVarsVisitor extends Visitor{

    ArrayList<Integer> numVars = new ArrayList<>();
    ArrayList<String> methodNames = new ArrayList<>();
    int numCurVars = 0;

    public Map<String,Integer> getNumLocalVars(Program ast){
        Map<String,Integer> numVarsMap = new HashMap<String,Integer>();
        for(int i = 0; i < numVars.size(); i++){
            numVarsMap.put(methodNames.get(i), numVars.get(i));
        }
        return numVarsMap;
    }

    public Object visit(Class_ node){
        super.visit(node);
        for(int i = 0; i < methodNames.size(); i++){
            methodNames.set(i, node.getName() + "." + methodNames.get(i));
        }
        node.getMemberList().accept(this);
        return null;
    }

    public Object visit(Method node){
        super.visit(node);
        node.getStmtList().accept(this);
        methodNames.add(node.getName());
        numVars.add(numCurVars);
        return null;
    }

    public Object visit(DeclStmt node){
        super.visit(node);
        numCurVars++;
        return null;

    }




}



