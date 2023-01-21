import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer in;
    private FileWriter out;

    private static int tabs = 0;

    public CompilationEngine(JackTokenizer input, FileWriter output) throws IOException {
        in = input;
        out = output;
    }

    public void compileClass() throws IOException {
        printXMLtype("class");
        tabs++;
        process("class");
        process(in.token);
        process("{");
        while(!in.token.equals("constructor") && !in.token.equals("function") && !in.token.equals("method")) {
            compileClassVarDec();
        }
        while(!in.token.equals("}")) {
            compileSubroutine();
        }
        process("}");
        tabs--;
        printXMLtype("/class");
    }

    public void compileClassVarDec() throws IOException {
        printXMLtype("classVarDec");
        tabs++;
        process(in.token); // field | static
        process(in.token); // type
        process(in.token); // varName
        while(in.token.equals(",")) {
            process(",");
            process(in.token);
        }
        process(";");
        tabs--;
        printXMLtype("/classVarDec");
    }

    public void compileSubroutine() throws IOException {
        printXMLtype("subroutineDec");
        tabs++;
        process(in.token); // constructor | function | method
        process(in.token); // void | type
        process(in.token); // subroutineName
        process("(");
        compileParameterList();
        process(")");
        compileSubroutineBody();
        tabs--;
        printXMLtype("/subroutineDec");
    }

    public void compileParameterList() throws IOException {
        printXMLtype("parameterList");
        if(!in.token.equals(")")) {
            tabs++;
            process(in.token); // type
            process(in.token); // varName
            while(in.token.equals(",")) { // checks for multiple variables
                process(",");
                process(in.token); // type
                process(in.token); // varName
            }
            tabs--;
        }
        printXMLtype("/parameterList");
        return;
    }

    public void compileSubroutineBody() throws IOException {
        printXMLtype("subroutineBody");
        tabs++;
        process("{");
        while(in.token.equals("var")){
            compileVarDec();
        }
        compileStatements();
        tabs--;
        process("}");
        printXMLtype("/subroutineBody");
        return;
    }

    public void compileVarDec() throws IOException {
        printXMLtype("varDec");
        tabs++;
        process("var"); // var
        process(in.token); // type
        process(in.token); // varName
        while(in.token.equals(",")) { // checks for multiple variables
            process(",");
            process(in.token);
        }
        process(";");
        tabs--;
        printXMLtype("/varDec");
        return;
    }

    public void compileStatements() throws IOException {
        printXMLtype("statements");
        tabs++;
        while(!in.token.equals("}")) {
            compileStatement();
        }
        tabs--;
        printXMLtype("/statements");
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

    public void compileLet() throws IOException {
        printXMLtype("letStatement");
        tabs++;
        process("let");
        process(in.token);
        if(in.token.equals("[")){
            process("[");
            compileExpression();
            process("]");
        }
        process("=");
        compileExpression();
        process(";");
        tabs--;
        printXMLtype("/letStatement");
    }

    public void compileIf() throws IOException {
        printXMLtype("ifStatement");
        tabs++;
        process("if");
        process("(");
        compileExpression();
        process(")");
        process("{");
        compileStatements();
        process("}");
        if (in.token.equals("else"))
        {
            process("else");
            process("{");
            compileStatements();
            process("}");
        }
        tabs--;
        printXMLtype("/ifStatement");
    }

    public void compileWhile() throws IOException {
        printXMLtype("whileStatement");
        tabs++;
        process("while");
        process("(");
        compileExpression();
        process(")");
        process("{");
        compileStatements();
        process("}");
        tabs--;
        printXMLtype("/whileStatement");
    }

    public void compileDo() throws IOException {
        printXMLtype("doStatement");
        tabs++;
        process("do");
        compileSubroutineCall();
        process(";");
        tabs--;
        printXMLtype("/doStatement");
    }

    public void compileReturn() throws IOException {
        printXMLtype("returnStatement");
        tabs++;
        process("return");
        if(!in.token.equals(";")) {
            compileExpression();
        }
        process(";");
        tabs--;
        printXMLtype("/returnStatement");
    }

    public void compileExpression() throws IOException {
        printXMLtype("expression");
        tabs++;
        compileTerm();
        while(in.isOp()) {
            if(in.token.equals(">")) {
                printXMLToken("&gt;", "symbol");
                in.advance();
            } else if (in.token.equals("<")) {
                printXMLToken("&lt;", "symbol");
                in.advance();
            } else if (in.token.equals("&")) {
                printXMLToken("&amp;", "symbol");
                in.advance();
            } else {
                process(in.token);
            }
            compileTerm();
        }
        tabs--;
        printXMLtype("/expression");
    }

    public void compileTerm() throws IOException {
        printXMLtype("term");
        tabs++;
        if(in.tokenType().equals("identifier")) {
            compileSubroutineCall();
        } else if(in.token.equals("-") || in.token.equals("~")) {
            process(in.token);
            compileTerm();
        } else if(in.token.equals("(")) {
            process("(");
            compileExpression();
            process(")");
        } else {
            if(in.tokenType().equals("stringConstant")) {
                printXMLToken(in.stringVal(), "stringConstant");
                in.advance();
            } else {
                process(in.token);
            }
        }
        tabs--;
        printXMLtype("/term");
    }

    public void compileSubroutineCall() throws IOException {
        String prev = in.token;
        String prevType = in.tokenType();
        in.advance();
        printXMLToken(prev, prevType);
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
        printXMLtype("expressionList");
        if(!in.token.equals(")")) {
            count++;
            tabs++;
            compileExpression();
            while(in.token.equals(",")) { // checks for multiple variables
                count++;
                process(",");
                compileExpression();
            }
            tabs--;
        }
        printXMLtype("/expressionList");
        return count;
    }

    private void process(String str) throws IOException {
        if(in.token.equals(str) || in.tokenType().equals("identifier")) {
            printXMLToken(str);
        } else {
            System.out.println("syntax error");
        }
        if(in.hasMoreTokens()) {
            in.advance();
        }
    }

    private void printXMLToken(String str) throws IOException {
        if(tabs != 0) {
            out.write('\t');
        }
        for(int i = 0; i < tabs - 1; i++) {
            out.append('\t');
        }
        out.append("<" + in.tokenType() + "> " + str + " </" +in.tokenType() + ">");
        out.write('\n');
    }

    private void printXMLToken(String str, String type) throws IOException {
        if(tabs != 0) {
            out.write('\t');
        }
        for(int i = 0; i < tabs - 1; i++) {
            out.append('\t');
        }
        out.append("<" + type + "> " + str + " </" + type + ">");
        out.write('\n');
    }

    private void printXMLtype(String type) throws IOException {
        if(tabs != 0) {
            out.write('\t');
        }
        for(int i = 0; i < tabs - 1; i++) {
            out.append('\t');
        }
        out.append("<" + type + ">");
        out.write('\n');
    }
}