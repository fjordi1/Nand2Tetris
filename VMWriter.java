import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private FileWriter out;
    private String fileName;

    // Constructor
    public VMWriter(FileWriter output) {
        this.out = output;
    }

    public void setFileName(String name) {
        fileName = name;
    }

    public void writeArithmethic(String cmd) {
            
    }

    public void writePush(String segment, int i) {
       
    }
    
    public void writePoph(String segment, int i) {
       
    }

    public void writeLabel(String label) {
        
    }

    public void writeGoto(String label) {
       
    }
    
    public void writeIf(String label) {
        
    }

    public void writeFunction(String name, int nVars) {
        
    }

    public void writeCall(String name, int nArgs) {
        
    }

    public void writeReturn() {
        
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
