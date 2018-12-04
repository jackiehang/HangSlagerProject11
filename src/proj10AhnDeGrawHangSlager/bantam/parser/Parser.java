/*
 * Authors: Haoyu Song and Dale Skrien
 * Date: Spring and Summer, 2018
 *
 * In the grammar below, the variables are enclosed in angle brackets.
 * The notation "::=" is used instead of "-->" to separate a variable from its rules.
 * The special character "|" is used to separate the rules for each variable.
 * All other symbols in the rules are terminals.
 * EMPTY indicates a rule with an empty right hand side.
 * All other terminal symbols that are in all caps correspond to keywords.
 */
package proj10AhnDeGrawHangSlager.bantam.parser;

import org.reactfx.value.Var;
import proj10AhnDeGrawHangSlager.bantam.ast.*;
import proj10AhnDeGrawHangSlager.bantam.lexer.Scanner;
import proj10AhnDeGrawHangSlager.bantam.lexer.Token;
import proj10AhnDeGrawHangSlager.bantam.util.Error;
import proj10AhnDeGrawHangSlager.bantam.util.ErrorHandler;
import proj10AhnDeGrawHangSlager.bantam.visitor.Visitor;

import static proj10AhnDeGrawHangSlager.bantam.lexer.Token.Kind;
import static proj10AhnDeGrawHangSlager.bantam.lexer.Token.Kind.*;

/**
 * This class constructs an AST from a legal Bantam Java program.  If the
 * program is illegal, then one or more error messages are displayed.
 */
public class Parser
{
    // instance variables
    private Scanner scanner;
    private Token currentToken; // the lookahead token
    private ErrorHandler errorHandler;

    // constructor
    public Parser(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }


    /**
     * parse the given file and return the root node of the AST
     * @param filename The name of the Bantam Java file to be parsed
     * @return The Program node forming the root of the AST generated by the parser
     */
    public Program parse(String filename) {
        this.scanner = new Scanner(filename, this.errorHandler);
        return parseProgram();
    }


    /*
     * <Program> ::= <Class> | <Class> <Program>
     */
    private Program parseProgram() {
        int position = currentToken.position;
        ClassList classList = new ClassList(position);

        while (currentToken.kind != EOF) {
            Class_ aClass = parseClass();
            classList.addElement(aClass);
        }

        return new Program(position, classList);
    }


    /*
	 * <Class> ::= CLASS <Identifier> <ExtendsClause> { <MemberList> }
     * <ExtendsClause> ::= EXTENDS <Identifier> | EMPTY
     * <MemberList> ::= EMPTY | <Member> <MemberList>
     */
    private Class_ parseClass() {
        if (this.currentToken.kind != CLASS) {
            this.errorHandler.register(Error.Kind.PARSE_ERROR, "INVALID CLASS DECLARATION");
            return null;
        }
        this.currentToken = scanner.scan();
        String left = parseIdentifier();

        this.currentToken = scanner.scan();
        String parent = null;
        if (this.currentToken.kind == EXTENDS) {
            this.currentToken = scanner.scan();
            parent = parseIdentifier();
        }
        this.currentToken = scanner.scan();
        if (this.currentToken.kind == LCURLY) {
            this.currentToken = scanner.scan();

            MemberList memberList = new MemberList(this.currentToken.position);
            Member member = parseMember();
            while (member != null) {
                memberList.addElement(member);
                this.currentToken = scanner.scan();
                member = parseMember();
            }
            return new Class_(this.currentToken.position, left.concat(".java"), left, parent, memberList);
        }
        else {
            this.errorHandler.register(Error.Kind.PARSE_ERROR, "INVALID CLASS DECLARATION");
            return null;
        }
    }


    /* Fields and Methods
     * <Member> ::= <Field> | <Method>
     * <Method> ::= <Type> <Identifier> ( <Parameters> ) <Block>
     * <Field> ::= <Type> <Identifier> <InitialValue> ;
     * <InitialValue> ::= EMPTY | = <Expression>
     */
     private Member parseMember() {
         String type = parseType();
         this.currentToken = scanner.scan();

         String name = parseIdentifier();
         this.currentToken = scanner.scan();

         if (this.currentToken.kind == LPAREN) {

             FormalList params = parseParameters();
             this.currentToken = scanner.scan();

             if (this.currentToken.kind == RPAREN) {
                 BlockStmt block = (BlockStmt)parseBlock();
                 return new Method(this.currentToken.position, type, name, params, block.getStmtList());
             }
             else {
                 this.errorHandler.register(Error.Kind.PARSE_ERROR, "INVALID MEMBER DECLARATION");
                 return null;
             }
         }
         else {
             Expr init = null;
             if (this.currentToken.kind == ASSIGN) {
                this.currentToken = scanner.scan();
                init = parseExpression();
             }
             return new Field(this.currentToken.position, type, name, init);
         }
     }


    //-----------------------------------

    /* Statements
     *  <Stmt> ::= <WhileStmt> | <ReturnStmt> | <BreakStmt> | <DeclStmt>
     *              | <ExpressionStmt> | <ForStmt> | <BlockStmt> | <IfStmt>
     */
     private Stmt parseStatement() {
            Stmt stmt;

            switch (currentToken.kind) {
                case IF:
                    stmt = parseIf();
                    break;
                case LCURLY:
                    stmt = parseBlock();
                    break;
                case VAR:
                    stmt = parseDeclStmt();
                    break;
                case RETURN:
                    stmt = parseReturn();
                    break;
                case FOR:
                    stmt = parseFor();
                    break;
                case WHILE:
                    stmt = parseWhile();
                    break;
                case BREAK:
                    stmt = parseBreak();
                    break;
                default:
                    stmt = parseExpressionStmt();
            }

            return stmt;
    }


    /*
     * <WhileStmt> ::= WHILE ( <Expression> ) <Stmt>
     */
    private Stmt parseWhile() {
        Expr expr = parseExpression();
        Stmt stmt = parseStatement();
        return new WhileStmt(this.currentToken.position,expr,stmt);
    }


    /*
     * <ReturnStmt> ::= RETURN <Expression> ; | RETURN ;
     */
	private Stmt parseReturn() {

        this.currentToken = scanner.scan();
        Expr right = null;
        while (this.currentToken.kind != SEMICOLON) {
            right = parseExpression();
        }
	    return new ReturnStmt(this.currentToken.position, right);
    }


    /*
	 * BreakStmt> ::= BREAK ;
     */
	private Stmt parseBreak() {
	    this.currentToken = scanner.scan();
	    return new BreakStmt(this.currentToken.position);
    }


    /*
	 * <ExpressionStmt> ::= <Expression> ;
     */
	private ExprStmt parseExpressionStmt() {
        this.currentToken = scanner.scan();
        Expr expr = parseExpression();
        return new ExprStmt(this.currentToken.position, expr);
    }


    /*
	 * <DeclStmt> ::= VAR <Identifier> = <Expression> ;
     * every local variable must be initialized
     */
	private Stmt parseDeclStmt() {


    }


    /*
	 * <ForStmt> ::= FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
     * <Start>     ::= EMPTY | <Expression>
     * <Terminate> ::= EMPTY | <Expression>
     * <Increment> ::= EMPTY | <Expression>
     */
	private Stmt parseFor() {
	    Stmt stmt = null;
        Expr initExpr = null;
        Expr predExpr = null;
        Expr updateExpr = null;

        while (!this.currentToken.getSpelling().equals(";")){
            this.currentToken = scanner.scan();
            initExpr = parseExpression();
        }

        while (!this.currentToken.getSpelling().equals(";")){
            this.currentToken = scanner.scan();
            predExpr = parseExpression();
        }

        while (this.currentToken.kind != RPAREN){
            this.currentToken = scanner.scan();
            updateExpr = parseExpression();
        }

        while(this.currentToken.kind != RBRACKET){
            this.currentToken = scanner.scan();
            stmt = parseStatement();
        }

        return new ForStmt(this.currentToken.position, initExpr, predExpr, updateExpr, stmt);
    }


    /*
	 * <BlockStmt> ::= { <Body> }
     * <Body> ::= EMPTY | <Stmt> <Body>
     */
	private Stmt parseBlock() {
        StmtList listOfNodes = new StmtList(this.currentToken.position);
        while(this.currentToken.kind != RBRACKET){
            currentToken = scanner.scan();
            listOfNodes.addElement(parseStatement());
        }
        return new BlockStmt(this.currentToken.position, listOfNodes);
    }


    /*
	 * <IfStmt> ::= IF ( <Expr> ) <Stmt> | IF ( <Expr> ) <Stmt> ELSE <Stmt>
     */
	private Stmt parseIf() {

	    Expr left = parseExpression();

	    this.currentToken = scanner.scan();
	    Stmt right = parseStatement();

        this.currentToken = scanner.scan();
        Stmt thenStmt = null;

        if (this.currentToken.kind == ELSE) {
            this.currentToken = scanner.scan();
	        thenStmt = parseStatement();
        }
        return new IfStmt(this.currentToken.position, left, right, thenStmt);
    }


    //-----------------------------------------
    // Expressions
    //Here we introduce the precedence to operations

    /*
	 * <Expression> ::= <LogicalOrExpr> <OptionalAssignment>
     * <OptionalAssignment> ::= EMPTY | = <Expression>
     */
	private Expr parseExpression() {
	    int position = currentToken.position;
        Expr right =null;
	    VarExpr left = (VarExpr)parseOrExpr();
	    while (this.currentToken.spelling.equals("=")){
	        this.currentToken = scanner.scan();
	        right = parseExpression();
        }

        if(right!=null){
            return new AssignExpr(this.currentToken.position,left.getRef().getExprType(), left.getName(), right  );
        }
        return left;
    }


    /*
	 * <LogicalOR> ::= <logicalAND> <LogicalORRest>
     * <LogicalORRest> ::= EMPTY |  || <LogicalAND> <LogicalORRest>
     */
	private Expr parseOrExpr() {
        int position = currentToken.position;

        Expr left = parseAndExpr();
        while (this.currentToken.spelling.equals("||")) {
            this.currentToken = scanner.scan();
            Expr right = parseAndExpr();
            left = new BinaryLogicOrExpr(position, left, right);
        }

        return left;
	}


    /*
	 * <LogicalAND> ::= <ComparisonExpr> <LogicalANDRest>
     * <LogicalANDRest> ::= EMPTY |  && <ComparisonExpr> <LogicalANDRest>
     */
	private Expr parseAndExpr() {
        int position = currentToken.position;

        Expr left = parseEqualityExpr();
        while (this.currentToken.spelling.equals("&&")) {
            this.currentToken = scanner.scan();
            Expr right = parseEqualityExpr();
            left = new BinaryLogicAndExpr(position, left, right);
        }

        return left;

    }


    /*
	 * <ComparisonExpr> ::= <RelationalExpr> <equalOrNotEqual> <RelationalExpr> |
     *                     <RelationalExpr>
     * <equalOrNotEqual> ::=  == | !=
     */
	private Expr parseEqualityExpr() {
        int position = this.currentToken.position;
        Expr left = parseRelationalExpr();
        if (this.currentToken.spelling.equals("==") || this.currentToken.spelling.equals("!=")) {
            this.currentToken = scanner.scan();
            Expr right = parseRelationalExpr();
            left = new BinaryLogicAndExpr(position, left, right);
        }
        return left;
    }


    /*
	 * <RelationalExpr> ::=<AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
     * <ComparisonOp> ::=  < | > | <= | >= | INSTANCEOF
     */
	private Expr parseRelationalExpr() {
        int position = currentToken.position;

        Expr left = parseAddExpr();
        while (this.currentToken.spelling.equals("+")) {
            this.currentToken = scanner.scan();
            Expr right = parseAddExpr();
            left = new BinaryLogicAndExpr(position, left, right);
        }

        return left;
    }


    /*
	 * <AddExpr>::＝ <MultExpr> <MoreMultExpr>
     * <MoreMultExpr> ::= EMPTY | + <MultExpr> <MoreMultExpr> | - <MultExpr> <MoreMultExpr>
     */
	private Expr parseAddExpr() {
        int position = currentToken.position;

        Expr left = parseMultExpr();
        while (this.currentToken.spelling.equals("-") || this.currentToken.spelling.equals("-")) {
            this.currentToken = scanner.scan();
            Expr right = parseMultExpr();
            left = new BinaryLogicAndExpr(position, left, right);
        }

        return left;
    }


    /*
	 * <MultiExpr> ::= <NewCastOrUnary> <MoreNCU>
     * <MoreNCU> ::= * <NewCastOrUnary> <MoreNCU> |
     *               / <NewCastOrUnary> <MoreNCU> |
     *               % <NewCastOrUnary> <MoreNCU> |
     *               EMPTY
     */
	private Expr parseMultExpr() { }

    /*
	 * <NewCastOrUnary> ::= < NewExpression> | <CastExpression> | <UnaryPrefix>
     */
	private Expr parseNewCastOrUnary() {
	    if (this.currentToken.kind == CAST) return parseCast();
	    else if (this.currentToken.kind == )
    }


    /*
	 * <NewExpression> ::= NEW <Identifier> ( ) | NEW <Identifier> [ <Expression> ]
     */
	private Expr parseNew() {

    }


    /*
	 * <CastExpression> ::= CAST ( <Type> , <Expression> )
     */
	private Expr parseCast() {
        if (this.currentToken.kind == LPAREN) {
            String left = parseType();
            this.currentToken = scanner.scan();
            if (this.currentToken.kind == COMMA) {
                this.currentToken = scanner.scan();
                Expr right = parseExpression();
                this.currentToken = scanner.scan();
                if (this.currentToken.kind == RPAREN) {
                    return new CastExpr(this.currentToken.position, left, right);
                }
            }
        }
        this.errorHandler.register(Error.Kind.PARSE_ERROR, "INVALID CAST EXPRESSION");
        return null;
    }


    /*
	 * <UnaryPrefix> ::= <PrefixOp> <UnaryPrefix> | <UnaryPostfix>
     * <PrefixOp> ::= - | ! | ++ | --
     */
	private Expr parseUnaryPrefix() { }


    /*
	 * <UnaryPostfix> ::= <Primary> <PostfixOp>
     * <PostfixOp> ::= ++ | -- | EMPTY
     */
	private Expr parseUnaryPostfix() {
	    Expr left = parsePrimary();
	    this.currentToken = scanner.scan();
	    Expr right = null;
	    switch(currentToken.kind) {
            case UNARYINCR: return new UnaryIncrExpr(this.currentToken.position, left, true);
            case UNARYDECR: return new UnaryDecrExpr(this.currentToken.position, left, true);
        }
        return left;
    }


    /*
	 * <Primary> ::= ( <Expression> ) | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> | <VarExpr> | <DispatchExpr>
     * <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
     * <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
     * <VarExprSuffix> ::= [ <Expr> ] | EMPTY
     * <DispatchExpr> ::= <DispatchExprPrefix> <Identifier> ( <Arguments> )
     * <DispatchExprPrefix> ::= <Primary> . | EMPTY
     */
	private Expr parsePrimary() {
	    if (this.currentToken.kind == LCURLY) {
	        return parseExpression();
        }
        switch(this.currentToken.kind) {
            case INTCONST: return parseIntConst();
            case BOOLEAN: return parseBoolean();
            case STRCONST: return parseStringConst();
            case VAR: return parseVarExpr();
        }
    }

    /*
     * <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
     * <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
     */
    private Expr parseVarExpr() {
//        Expr left = parseVarExprPrefix();
//        Expr middle = parseIdentifier();
//        Expr right = parseVarExprSuffix();
    }

    /*
     * <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
     * <VarExprSuffix> ::= [ <Expr> ] | EMPTY
     * <DispatchExpr> ::= <DispatchExprPrefix> <Identifier> ( <Arguments> )
     * <DispatchExprPrefix> ::= <Primary> . | EMPTY
     */
    private String parseVarExprPrefix() {
        if (this.currentToken.kind == IDENTIFIER) {
            return parseIdentifier();
        }
        return null;
    }

    /*
	 * <Arguments> ::= EMPTY | <Expression> <MoreArgs>
     * <MoreArgs>  ::= EMPTY | , <Expression> <MoreArgs>
     */
	private ExprList parseArguments() {
	    ExprList eList = new ExprList(this.currentToken.position);
	    Expr expr = parseExpression();
	    eList.addElement(expr);

        this.currentToken = scanner.scan();
	    while(this.currentToken.kind == COMMA){
	        this.currentToken = scanner.scan();
	        eList.addElement(parseExpression());
        }
	    return new ExprList(this.currentToken.position);
    }


    /*
	 * <Parameters>  ::= EMPTY | <Formal> <MoreFormals>
     * <MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
     */
	private FormalList parseParameters() {

	    FormalList formalList = new FormalList(this.currentToken.position);
	    Formal left = parseFormal();
	    formalList.addElement(left);

        this.currentToken = scanner.scan();
	    while (this.currentToken.kind == COMMA) {
            formalList.addElement(parseFormal());
            this.currentToken = scanner.scan();
        }
        return formalList;
    }


    /*
	 * <Formal> ::= <Type> <Identifier>
     */
	private Formal parseFormal() {
	    String type = parseType();
	    this.currentToken = scanner.scan();
        String identifier = parseIdentifier();
        return new Formal(this.currentToken.position, type, identifier);
    }


    /*
	 * <Type> ::= <Identifier> <Brackets>
     * <Brackets> ::= EMPTY | [ ]
     */
	private String parseType() {
        String s = parseIdentifier();

        this.currentToken = scanner.scan();
        if (this.currentToken.kind == LBRACKET) {
            this.currentToken = scanner.scan();
            if (this.currentToken.kind == RBRACKET) {
                s = s.concat("[]");
                return s;
            }
        }
        return s;
    }


    //----------------------------------------
    //Terminals

	private String parseOperator() {

        Kind kind = this.currentToken.kind;
        if (kind == BINARYLOGIC || kind == PLUSMINUS || kind ==  MULDIV || kind == COMPARE
            || kind == UNARYDECR || kind == UNARYINCR || kind == ASSIGN || kind == UNARYNOT) {

            return this.currentToken.getSpelling();
        }
        return null;
    }


    private String parseIdentifier() {
        if (this.currentToken.kind == IDENTIFIER) {
            return this.currentToken.getSpelling();
        }
        return null;
    }


    private ConstStringExpr parseStringConst() {
	    if (this.currentToken.kind == STRCONST) {
	        return new ConstStringExpr(this.currentToken.position, this.currentToken.getSpelling());
        }
        return null;
    }


    private ConstIntExpr parseIntConst() {
	    if (this.currentToken.kind == INTCONST) {
	        return new ConstIntExpr(this.currentToken.position, this.currentToken.getSpelling());
        }
        return null;
    }


    private ConstBooleanExpr parseBoolean() {
	    if (this.currentToken.kind == BOOLEAN) {
	        return new ConstBooleanExpr(this.currentToken.position, this.currentToken.getSpelling());
        }
        return null;
    }

}