import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer in;
    private VMWriter writer;
    private SymbolTable symbols;
    private String currType;
    private String currSubroutine;
    private int labelIndex;
    private String ClassName;

    public CompilationEngine(JackTokenizer token, FileWriter output) throws IOException {
        in = token;
        writer = new VMWriter(output);
        symbols = new SymbolTable();

        labelIndex = 0;
    }

    public void compileClass() throws IOException {
        process("class");
        ClassName = in.token; // used to save current class name
        process(in.token); // class name
        process("{");
        while(!in.token.equals("constructor") && !in.token.equals("function") && !in.token.equals("method")) {
            compileClassVarDec();
        }
        while(!in.token.equals("}")) {
            compileSubroutine();
        }
        process("}");
    }

    public void compileClassVarDec() throws IOException {
        if (!in.token.equals("field") && !in.token.equals("static")){
            return;
        }
            String _class = in.token; // get the class of the variable (field / static)
            in.advance();
            String type = in.token; // get the type of the variable
            in.advance();
        while(!in.token.equals(";")){
            String varName = in.token; // get the name of the variable 
            symbols.define(varName, type, _class); // adds the variable to classSym table
            writer.writePush(_class, symbols.varCount(_class.toUpperCase())-1); // push _class x , varCount-1 since define inc. index by 1.
            in.advance();
            if(in.token.equals(",")){
                in.advance();
            }
        }
    }

    public void compileSubroutine() throws IOException {
        symbols.reset();
        currType = in.token; // used to check if keyword is method or function/constructor
        process(in.token); // constructor | function | method
        process(in.token); // void | type
        currSubroutine = in.token; // used to save current subroutineName
        process(in.token); // subroutineName
        process("(");
        compileParameterList();
        process(")");
        compileSubroutineBody();
    }

    public void compileParameterList() throws IOException {
        in.advance();
        if(!in.token.equals(")")) {
            String type = in.token; // type
            in.advance();
            String varName = in.token; // varName
            symbols.define(varName, type, "ARG");
            while(in.token.equals(",")) { // checks for multiple variables
                process(",");
                in.advance(); // type
                type = in.token;
                in.advance(); // varName
                varName = in.token;
                in.advance();
                symbols.define(varName, type, "ARG");
            }
        }
    }

    public void compileSubroutineBody() throws IOException {
        process("{");
        compileVarDec();
        String funName = ClassName + "." + in.token;
        in.advance();
        int nVars = Integer.parseInt(in.token);
        writer.writeFunction(funName,nVars);
        //write VM function declaration
        if (currType.equals("method")){
            writer.writePush("ARG",0);
            writer.writePop("POINTER",0);
        }
        else if(currType.equals("function") || currType.equals("constructor")){
            writer.writePush("constant", symbols.varCount("FIELD"));
            writer.writeCall("Memory.alloc", 1);
            writer.writePop("POINTER",0);
        }
        compileStatement();
        process("}");
    }

    public void compileVarDec() throws IOException {
        if (!in.token.equals("var")){
            return;
        }
            in.advance();
            String type = in.token; // get the type of the variable
            in.advance();
        while(!in.token.equals(";")){
            String varName = in.token; // get the name of the variable
            symbols.define(varName, type, "local"); // insert it into the subroutineSym Table
            writer.writePush("local", symbols.varCount("VAR")-1); // push local x , varCount-1 since define inc. index by 1.
            in.advance();
            if(in.token.equals(",")){ // checks for more variables of the same type
                in.advance();
            }
        }
    }

    public void compileStatements() throws IOException {
        while(!in.token.equals("}")) {
            compileStatement();
        }
    }

    public void compileStatement() throws IOException {
        switch (in.token) {
            case "while":
                compileWhile();
                break;
            case "let":
                compileLet();
                break;
            case "if":
                compileIf();
                break;
            case "do":
                compileDo();
                break;
            case "return":
                compileReturn();
                break;
            default:
                break;
        }
    }

    // let arr[2] = 17
    public void compileLet() throws IOException {
        //varName
        in.advance();
        String varName = in.identifier();

        // '=' or '['
        boolean isArr = false;
        in.advance();
        if(in.token.equals("[")) {
            isArr = true;

            // push arr
            writer.writePush(symbols.kindOf(varName), symbols.indexOf(varName));

            // push 2
            compileExpression();

            process("]");

            // add
            writer.writeArithmethic("ADD");
        }

        // expression - push 17
        in.advance();
        compileExpression();

        // ;
        process(";");

        if(isArr) {
            writer.writePop("TEMP", 0);
            writer.writePop("POINTER", 1);
            writer.writePush("TEMP", 0);
            writer.writePop("THAT", 0);
        } else {
            writer.writePop(symbols.kindOf(varName), symbols.indexOf(varName));
        }

    }

    public void compileIf() throws IOException {
        String elseLabel = newLabel();
        String endLabel = newLabel();

        // if (expresssion) {statement}
        // else {statement}
        process("(");

        compileExpression();

        process(")");

        writer.writeArithmethic("NOT");
        writer.writeIf(elseLabel);

        process("{");

        compileStatement();

        process("}");

        writer.writeGoto(endLabel);

        writer.writeLabel(elseLabel);

        in.advance();
        if(in.token.equals("else")) {
            process("{");
            compileStatement();
            process("}");
        } 

        writer.writeLabel(endLabel);
    }

    public void compileWhile() throws IOException {
        // while(expression) {statements}
        String startLabel = newLabel();
        String endLabel = newLabel();

        writer.writeLabel(startLabel);

        process("(");

        compileExpression();

        process(")");

        process("{");

        writer.writeArithmethic("NOT");

        writer.writeIf(endLabel);

        compileStatements();

        writer.writeGoto(startLabel);

        writer.writeLabel(endLabel);
    }

    public void compileDo() throws IOException {
        // Handle subroutine call
        compileSubroutineCall();

        // Make sure it is closed
        process(";");

        // Pop return value
        writer.writePop("TEMP", 0);
    }

    public void compileReturn() throws IOException {
        in.advance();

        // If expression
        if (in.token.equals(";")){
            writer.writePush("CONST",0);
        }else {
            //expression
            compileExpression();
            //';'
            process(";");
        }

        writer.writeReturn();
    }

    public void compileExpression() throws IOException {
        
    }

    public void compileTerm() throws IOException {
        
    }

    public void compileSubroutineCall() throws IOException {
        // TODO *****************************
        String prev = in.token;
        String prevType = in.tokenType();
        in.advance();
        if(in.token.equals(".")) {
            process(".");
            process(in.token);
            process("(");
            compileExpressionList();
            process(")");
        } else if(in.token.equals("[")) {
            process("[");
            compileExpression();
            process("]");
        } else if(in.token.equals("(")) {
            process("(");
            compileExpressionList();
            process(")");
        }
    }

    public int compileExpressionList() throws IOException {
        int count = 0;
        if(!in.token.equals(")")) {
            count++;
            compileExpression();
            while(in.token.equals(",")) { // checks for multiple variables
                count++;
                process(",");
                compileExpression();
            }
        }
        return count;
    }

    private void process(String str) {
        if(!in.token.equals(str)) {
            throw new IllegalStateException("Expected token: " + str + " Current token: " + in.token);
        }
        if(in.hasMoreTokens()) {
            in.advance();
        }
    }

    private String newLabel(){
        return "L" + (labelIndex++);
    }

}