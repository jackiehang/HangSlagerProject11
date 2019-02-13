package proj11HangSlager.bantam;
import proj11HangSlager.bantam.ast.*;
import proj11HangSlager.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StringConstantsVisitor extends Visitor {

    ArrayList<String> stringList = new ArrayList<>();

    public Map<String, String> visit(Program ast) {
        Map<String,String> stringMap = new HashMap<String,String>();
        ast.accept(this);
        int stringNum = stringList.size();
        for(int i = 0; i < stringNum; i++){
            String strConstName = "StringConst_" + Integer.toString(i);
            stringMap.put(strConstName, stringList.get(i));
        }
        return stringMap;
    }

    public Object visit(ConstStringExpr node){
        stringList.add(node.getConstant());
        return null;
    }
}
