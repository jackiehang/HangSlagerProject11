package proj11HangSlager.bantam;
import proj11HangSlager.bantam.ast.*;
import proj11HangSlager.bantam.visitor.Visitor;

public class MainMainVisitor extends Visitor{

    boolean hasBeenFound = false;


    public boolean hasMain(Program ast) {
        ast.accept(this);
        return hasBeenFound;
    }
    public Object visit(Class_ node){
        super.visit(node);
        if(node.getName().equals("Main")){
            node.getMemberList().accept(this);
        }

        return null;
    }
    public Object visit(Method node) {
        super.visit(node);
        if((node.getName().equals("main")) && (node.getFormalList().getSize() == 0) && (node.getReturnType().equals("void"))) {
            hasBeenFound = true;
        }
        return null;
    }
}