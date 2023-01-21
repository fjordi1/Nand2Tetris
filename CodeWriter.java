import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
    private FileWriter out;
    private String fileName;
    private static int funNum = 0;
    private static int boolNum = 0;
    // Constructor
    public CodeWriter(FileWriter output) {
        this.out = output;
    }

    public void setFileName(String name) {
        fileName = name;
    }

    public void writeArithmethic(String cmd) {
        if(cmd.equals("add")) {
            writeLine("@SP"); //258
            writeLine("A=M-1"); // 257
            writeLine("D=M"); // D=8
            writeLine("@SP"); // 258
            writeLine("M=M-1"); // SP = 257  
            writeLine("A=M-1"); // A = 257
            writeLine("D=D+M");  
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=D");
        }
        if(cmd.equals("sub")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("D=M");
            writeLine("@SP");
            writeLine("M=M-1");   
            writeLine("A=M-1");
            writeLine("D=M-D");
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=D");
        }
        if(cmd.equals("neg")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=-M"); 
        }
        if(cmd.equals("eq")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("D=M");
            writeLine("@SP");
            writeLine("M=M-1");
            writeLine("A=M-1");
            writeLine("D=D-M");
            writeLine("M=-1");
            writeLine("@bool"+boolNum);
            writeLine("D;JEQ");
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=M+1");
            writeLine("(bool"+boolNum+")");
            boolNum++;
        }
        if(cmd.equals("lt")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("D=M");
            writeLine("@SP");
            writeLine("M=M-1");
            writeLine("A=M-1");
            writeLine("D=D-M"); // y - x
            writeLine("M=-1");
            writeLine("@bool"+boolNum);
            writeLine("D;JGT");
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=M+1");
            writeLine("(bool"+boolNum+")");
            boolNum++;
        }
        if(cmd.equals("gt")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("D=M");
            writeLine("@SP");
            writeLine("M=M-1");
            writeLine("A=M-1");
            writeLine("D=D-M"); // y - x
            writeLine("M=-1");
            writeLine("@bool"+boolNum);
            writeLine("D;JLT");
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=M+1");
            writeLine("(bool"+boolNum+")");
            boolNum++;
        }
        if(cmd.equals("and")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("D=M"); // D = y
            writeLine("@SP");
            writeLine("M=M-1");
            writeLine("A=M-1"); // A = x
            writeLine("D=D&M");
            writeLine("M=D");
        }
        if(cmd.equals("or")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("D=M"); // D = y
            writeLine("@SP");
            writeLine("M=M-1");
            writeLine("A=M-1"); // A = x
            writeLine("D=D|M");
            writeLine("M=D");
        }
        if(cmd.equals("not")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=!M");
        }

    }

    public void writePushPop(String cmd, String segment, int i) {
        String at;
        switch (segment) {
            case "constant":
                at = "SP";
                break;
            case "local":
                at = "LCL";
                break;
            case "argument":
                at = "ARG";
                break;
            case "this":
                at = "THIS";
                break;
            case "that":
                at = "THAT";
                break;
            case "static":
                at = fileName + "." + i;
                break;
            case "pointer":
                if(i == 0)
                    at = "THIS";
                else
                    at = "THAT";
                break;
            //temp
            default:
                int num = 5+i;
                at = "" + num;
                break;
        }
        if(cmd.equals("push")) {
            if(segment.equals("constant")) {
                // constant
                writeLine("@" + i);
                writeLine("D=A");
                writeLine("@SP");
                writeLine("A=M");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("M=M+1");
            } else if(segment.equals("pointer") || segment.equals("static") || segment.equals("temp")) {
                // pointer, static, temp
                writeLine("@" + at);
                writeLine("D=M");
                writeLine("@SP");
                writeLine("A=M");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("M=M+1");
            } else {
                // this, that, arg, local
                writeLine("@" + i);
                writeLine("D=A");
                writeLine("@"+ at);
                writeLine("A=M+D"); // addr < LCL + i
                writeLine("D=M"); // D = RAM[addr]
                writeLine("@SP");
                writeLine("A=M");
                writeLine("M=D"); // RAM[SP] < RAM[addr]
                writeLine("@SP");
                writeLine("M=M+1");
            } 
        } else { //POP
            if(segment.equals("this") || segment.equals("that") || segment.equals("argument") || segment.equals("local")) {
                writeLine("@" + i);
                writeLine("D=A");
                writeLine("@"+ at);
                writeLine("A=M+D"); // A < LCL + i
                writeLine("D=A");
                writeLine("@R13");
                writeLine("M=D");
                writeLine("@SP");
                writeLine("M=M-1"); // SP--
                writeLine("A=M");
                writeLine("D=M"); // RAM[SP]
                writeLine("@R13");
                writeLine("A=M");
                writeLine("M=D");
                writeLine("@R13");
                writeLine("M=0");   
            } else {
                writeLine("@SP");
                writeLine("M=M-1"); // SP--
                writeLine("A=M");
                writeLine("D=M"); // D = RAM[SP]
                writeLine("@" + at); // @Foo.i
                writeLine("M=D"); // RAM[Foo.i] = RAM[SP]
            }
        }
    }

    public void writeLabel(String label) {
        writeLine("("+label+")");
    }

    public void writeGoto(String label) {
        writeLine("@"+label);
        writeLine("0;JMP");
    }
    
    public void writeIf(String label) {
        writeLine("@SP");
        writeLine("A=M-1");
        writeLine("D=M");
        writeLine("@SP");
        writeLine("M=M-1");
        writeLine("@"+label);
        writeLine("D;JNE");
    }

    public void writeFunction(String functionName, int nArgs) {
        writeLabel(functionName);
        for(int i = 0; i < nArgs; i++) {
            writePushPop("push", "constant", 0);
        }
    }

    public void writeCall(String functionName, int nArgs) {
        writeLine("@"+ functionName + "$ret." + funNum); // Push Return address
        writeLine("D=A");
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("M=M+1"); 
        writeLine("@LCL"); // Push LCL address
        writeLine("D=M");
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("M=M+1"); 
        writeLine("@ARG"); // Push ARG address
        writeLine("D=M");
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("M=M+1"); 
        writeLine("@THIS"); // Push THIS address
        writeLine("D=M");
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("M=M+1"); 
        writeLine("@THAT"); // Push THAT address
        writeLine("D=M");
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("M=M+1"); 
        int newArg = 5+nArgs;
        writeLine("@SP"); // Reposition ARG
        writeLine("D=M");
        writeLine("@"+newArg);
        writeLine("D=D-A");
        writeLine("@ARG");
        writeLine("M=D");
        writeLine("@SP"); // Reposition LCL
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");
        writeGoto(functionName); // Transfers control to the callee
        writeLabel(functionName + "$ret." + funNum); // Injects label into the code
        funNum++;
    }

    public void writeReturn() {
        writeLine("@LCL"); // endFrame = LCL
        writeLine("D=M");
        writeLine("@R13");
        writeLine("M=D");
        writeLine("@5"); // retAddr = *(endFrame - 5)
        writeLine("D=D-A");
        writeLine("A=D");
        writeLine("D=M");
        writeLine("@R14");
        writeLine("M=D");
        writeLine("@SP"); // Replace arg[0] with return value
        writeLine("A=M-1");
        writeLine("D=M");
        writeLine("@ARG");
        writeLine("A=M");
        writeLine("M=D");
        writeLine("D=A+1"); // SP = ARG + 1
        writeLine("@SP");
        writeLine("M=D");
        writeLine("@R13"); // endFrame--
        writeLine("M=M-1"); 
        writeLine("A=M");
        writeLine("D=M"); // D = endFrame
        writeLine("@THAT"); // THAT = *(endFrame)
        writeLine("M=D");
        writeLine("@R13"); // endFrame--
        writeLine("M=M-1"); 
        writeLine("A=M");
        writeLine("D=M"); // D = endFrame
        writeLine("@THIS"); // THIS = *(endFrame)
        writeLine("M=D");
        writeLine("@R13"); // endFrame--
        writeLine("M=M-1"); 
        writeLine("A=M");
        writeLine("D=M"); // D = endFrame
        writeLine("@ARG"); // ARG = *(endFrame)
        writeLine("M=D");
        writeLine("@R13"); // endFrame--
        writeLine("M=M-1"); 
        writeLine("A=M");
        writeLine("D=M"); // D = endFrame
        writeLine("@LCL"); // LCL = *(endFrame)
        writeLine("M=D");
        writeLine("@R14");// goto retAddr
        writeLine("A=M");
        writeLine("0;JMP"); 
    }

    public void writeBootstrap() {
        writeLine("@256"); // SP = 256
        writeLine("D=A");
        writeLine("@SP");
        writeLine("M=D");
        writeCall("Sys.init", 0);
    }

    // Auxilary function
    private void writeLine(String line) {
        try {
        out.append(line);
        out.append('\n');
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
