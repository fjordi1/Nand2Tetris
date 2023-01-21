import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private FileWriter out;

    // Constructor
    public VMWriter(FileWriter output) {
        this.out = output;
    }

    public void writeArithmethic(String cmd) {
        write(cmd.toLowerCase());
    }

    public void writePush(String segment, int i) {
        write("push", getSegment(segment), String.valueOf(i));
    }
    
    public void writePoph(String segment, int i) {
        write("pop", getSegment(segment), String.valueOf(i));
    }

    public void writeLabel(String label) {
        write("label", label);
    }

    public void writeGoto(String label) {
        write("goto", label);
    }
    
    public void writeIf(String label) {
        write("if-goto", label);
    }

    public void writeFunction(String name, int nVars) {
        write("function", name, String.valueOf(nVars));
    }

    public void writeCall(String name, int nArgs) {
        write("call", name, String.valueOf(nArgs));
    }

    public void writeReturn() {
        write("return");
    }

    // Auxilary function that writes to file
    private void write(String cmd, String arg1, String arg2) {
        try {
            out.append(cmd + " " + arg1 + " " + arg2);
            out.append('\n');
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void write(String cmd, String arg1) {
        try {
            out.append(cmd + " " + arg1);
            out.append('\n');
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void write(String cmd) {
        try {
            out.append(cmd);
            out.append('\n');
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    // Auxilary function to get the right segment value
    public String getSegment(String seg) {
        if(seg.equals("CONST")) {
            return "constant";
        } else if (seg.equals("ARG")) {
            return "argument";
        } else {
            return seg.toLowerCase();
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
