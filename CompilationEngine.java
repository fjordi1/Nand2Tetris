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
            //writer.writePush(_class, symbols.varCount(_class.toUpperCase())-1); // push class x , varCount-1 since define inc. index by 1.
            in.advance();
            if(in.token.equals(",")){
                in.advance();
            }
        }
        process(";");
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
        if(!in.token.equals(")")) {
            String type = in.token; // type
            process(type);
            String varName = in.token; // varName
            symbols.define(varName, type, "ARG");
            process(varName);
            while(in.token.equals(",")) { // checks for multiple variables
                process(",");
                type = in.token;
                process(type); // varName
                varName = in.token;
                process(varName);
                symbols.define(varName, type, "ARG");
            }
        }
    }

    public void compileSubroutineBody() throws IOException {
        process("{");
        while(in.token.equals("var")) {
            compileVarDec();
        }

        // Write function declaration after knowing how many vars you have
        writer.writeFunction(ClassName + "." + currSubroutine ,symbols.varCount("VAR"));
        
        // Different actions for method and consturctor
        if (currType.equals("method")){
            writer.writePush("ARG", 0);
            writer.writePop("POINTER",0);

        }else if (currType.equals("constructor")){
            writer.writePush("CONST",symbols.varCount("FIELD"));
            writer.writeCall("Memory.alloc", 1);
            writer.writePop("POINTER",0);
        }

        compileStatements();
        process("}");
    }

    public void compileVarDec() throws IOException {
        if (!in.token.equals("var")){
            return;
        }
            process("var");
            String type = in.token; // get the type of the variable
            process(type);
        while(!in.token.equals(";")){
            String varName = in.token; // get the name of the variable
            symbols.define(varName, type, "local"); // insert it into the subroutineSym Table
            //writer.writePush("local", symbols.varCount("VAR")-1); // push local x , varCount-1 since define inc. index by 1.
            process(varName);
            if(in.token.equals(",")){ // checks for more variables of the same type
                process(",");
            }
        }
        process(";");
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
        process("let");
        String varName = in.identifier();

        // '=' or '['
        boolean isArr = false;
        process(varName);
        if(in.token.equals("[")) {
            process("[");
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
        process("=");
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
        process("if");
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

        compileStatements();

        process("}");

        writer.writeGoto(endLabel);

        writer.writeLabel(elseLabel);

        if(in.token.equals("else")) {
            process("else");
            process("{");
            compileStatements();
            process("}");
        } 

        writer.writeLabel(endLabel);
    }

    public void compileWhile() throws IOException {
        process("while");
        // while(expression) {statements}
        String endLabel = newLabel();
        String startLabel = newLabel();
        

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

        process("}");
    }

    public void compileDo() throws IOException {
        process("do");
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
            
        }
        process(";");
        writer.writeReturn();
    }

    public void compileExpression() throws IOException {
        compileTerm(); // term  

                        // op term
        String Op = "";
        while(in.isOp()) {
            switch (in.symbol()){
                case '+': {
                    Op = "ADD";
                    break;
                }
                case '-': {
                    Op = "SUB";
                    break;
                }
                case '*': {
                    Op = "Math.multiply";
                    break;
                }
                case '/': {
                    Op = "Math.divide";
                    break;
                }
                case '>': {
                    Op = "GT";
                    break;
                }
                case '<': {
                    Op = "LT";
                    break;
                }
                case '=': {
                    Op = "EQ";
                    break;
                }
                case '&': {
                    Op = "AND";
                    break;
                }
                case '|': {
                    Op = "OR";
                    break;
                }
            }
            in.advance();
            compileTerm();

            if(Op.equals("Math.multiply") || Op.equals("Math.divide")){
                writer.writeCall(Op, 2);
            }
            else{
                writer.writeArithmethic(Op);
            }
        }
    }

    public void compileTerm() throws IOException {
         
        if(in.tokenType().equals("identifier")) {
            String varName = in.token;
            process(varName);
            if(in.token.equals("[")) {
                process("[");
                writer.writePush(symbols.kindOf(varName), symbols.indexOf(varName));
                compileExpression();
                process("]");
                writer.writeArithmethic("ADD");
                writer.writePop("POINTER", 1);
                writer.writePush("THAT", 0);
            } else if (in.token.equals("(") || in.token.equals(".")) {
                compileSubroutineCall(varName);
            } else {
                writer.writePush(symbols.kindOf(varName), symbols.indexOf(varName));
            }
            
        } else if(in.token.equals("-") || in.token.equals("~")) {
            char c = in.symbol();
            process("" + c);
            compileTerm();
            if (c == '-'){
                writer.writeArithmethic("NEG");
            }
            else {
                writer.writeArithmethic("NOT");
            }
            
        } else if(in.token.equals("(")) {
            process("(");
            compileExpression();
            process(")");
        } else if(in.tokenType().equals("stringConstant")) {
            String str = in.stringVal();
            writer.writePush("CONST",str.length());
            writer.writeCall("String.new",1);

            for (int i = 0; i < str.length(); i++){
                writer.writePush("CONST",(int)str.charAt(i));
                writer.writeCall("String.appendChar",2);
            }
            in.advance();
        } else if(in.tokenType().equals("integerConstant")) {
            writer.writePush("CONST", in.intVal());
            in.advance();
        } else if(in.tokenType().equals("keyword")){
            switch(in.keyWord()){
                case "THIS":{ 
                    writer.writePush("POINTER",0);
                    break;
                }
                case "TRUE": {
                    writer.writePush("CONST",0);
                    writer.writeArithmethic("NOT");
                    break;
                }
                case "FALSE":{
                    writer.writePush("CONST",0);
                    break;
                }
                case "NULL":{
                    writer.writePush("CONST",0);
                    break;
                }
            }
            in.advance();
        }
    }

    // subroutineName '(' expressionList ')' | (className|varName) '.' subroutineName '(' expressionList ')'
    public void compileSubroutineCall() throws IOException {
        int nArgs = 0;
        String subName = in.token;
        in.advance();
        if(in.token.equals("(")) {
            writer.writePush("POINTER", 0); // push 'this' pointer
            process("(");
            nArgs += compileExpressionList();
            process(")");
            writer.writeCall(ClassName + "." + subName, nArgs);
        }
        else if(in.token.equals(".")) {
            process(".");
            String objName = subName;
            subName = in.token;
            process(in.token);
            
            String type = symbols.typeOf(objName);
            String finalName = "";

            if(type.equals("")) {
                finalName = objName + "." + subName;
            } else {
                writer.writePush(symbols.kindOf(objName), symbols.indexOf(objName));
                finalName = objName + "." + subName;
                
            }
            process("(");
            nArgs += compileExpressionList();
            process(")");
            writer.writeCall(finalName, nArgs);
        }
    }

    public void compileSubroutineCall(String subName) throws IOException {
        int nArgs = 0;
        if(in.token.equals(".")) {
            process(".");
            String objName = subName;
            subName = in.token;
            process(in.token);
            process("(");
            nArgs += compileExpressionList();
            process(")");
            String type = symbols.typeOf(objName);
            if(type.equals("")) {
                writer.writeCall(objName + "." + subName, nArgs);
            } else {
                writer.writePush(symbols.kindOf(objName), symbols.indexOf(objName));
                writer.writeCall(symbols.typeOf(objName) + "." + subName, nArgs);
            }
            
        } else if(in.token.equals("(")) {
            writer.writePush("POINTER", 0); // push 'this' pointer
            process("(");
            nArgs += compileExpressionList();
            process(")");
            writer.writeCall(ClassName + "." + subName, nArgs);
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
            //throw new IllegalStateException("Expected token: " + str + " Current token: " + in.token);
            System.out.println("Expected token: " + str + " Current token: " + in.token);
        }
        if(in.hasMoreTokens()) {
            in.advance();
        }
    }

    private String newLabel(){
        return "L" + (labelIndex++);
    }

}