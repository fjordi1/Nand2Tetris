import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer in;
    private VMWriter writer;
    private SymbolTable symbols;
    private String currClass;
    private String currSubroutine;
    private int labelIndex;

    public CompilationEngine(JackTokenizer token, FileWriter output) throws IOException {
        in = token;
        writer = new VMWriter(output);
        symbols = new SymbolTable();

        labelIndex = 0;
    }

    public void compileClass() throws IOException {
        
    }

    public void compileClassVarDec() throws IOException {
        
    }

    public void compileSubroutine() throws IOException {
        
    }

    public void compileParameterList() throws IOException {
        
    }

    public void compileSubroutineBody() throws IOException {
        
    }

    public void compileVarDec() throws IOException {
        if (!in.token.equals("var")){
            return;
        }
            in.advance();
            String type = in.token;
            in.advance();
        while(!in.token.equals(";")){
            String varName = in.token;
            symbols.define(varName, type, "local");
            writer.writePush("local", symbols.varCount("VAR")-1);
            in.advance();
            if(in.token.equals(",")){
                in.advance();
            }
        }
    }

    public void compileStatements() throws IOException {
        
    }

    public void compileStatement() throws IOException {
        
    }

    public void compileLet() throws IOException {
        //varName
        in.advance();
        String varName = in.identifier();

        // '=' or '['
        boolean isArr = false;
        in.advance();
        if(in.token.equals("[")) {
            isArr = true;

            writer.writePush(symbols.kindOf(varName), symbols.indexOf(varName));

            compileExpression();

            process("]");

            writer.writeArithmethic("ADD");
        }

        // expression
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
            writer.writePop(getSegment(symbols.kindOf(varName)), symbols.indexOf(varName));
        }

    }

    public void compileIf() throws IOException {
        
    }

    public void compileWhile() throws IOException {
        
    }

    public void compileDo() throws IOException {
        
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
        
    }

    public int compileExpressionList() throws IOException {
        
    }

    private void process(String str) {
        in.advance();
        if(!in.token.equals(str)) {
            throw new IllegalStateException("Expected token: " + str + " Current token: " + in.token);
        }
    }
}