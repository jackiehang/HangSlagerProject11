package proj11HangSlager.bantam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import proj11HangSlager.bantam.ast.*;
import proj11HangSlager.bantam.visitor.Visitor;



public class NumLocalVarsVisitor extends Visitor{



    public Map<String,Integer> getNumLocalVars(Program ast){
        ArrayList<Integer> numVars = new ArrayList<>();
        Map<String,Integer> numVarsMap = new HashMap<String,Integer>();




        return numVarsMap;

    }

    public Object visit(Class_ node){

        return null;
    }

    public Object visit(Method node){
        return null;
    }

    public Object visit(DeclStmt node){

    }




}










    ClassList classList = ast.getClassList();
        for(int i = 0; i < classList.getSize(); i++){
        int numFields = 0;
        Class_ currentClass = (Class_)classList.get(i);
        MemberList memberList = currentClass.getMemberList();
        for(int j = 0; j < memberList.getSize(); j++){
        if(memberList.get(j) instanceof Field){
        numFields++;
        }
        }
        numVarsMap.put(currentClass.getName()+"."+memberList.get(j));
        }