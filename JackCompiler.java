import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/* 
    • The top-most / main module
    • Input: a single fileName.jack, or a folder containing 0 or more such files
    • For each file:
    1. Creates a JackTokenizer from fileName.jack
    2. Creates an output file named fileName.xml
    3. Creates a CompilationEngine, and calls the compileClass method.
    */
public class JackCompiler {
    public static void main(String [] args) {
        File file = new File(args[0]);
        FileWriter out;
        String outputName;
        List<File> fileList = new LinkedList<File>();
        JackTokenizer jack;
        CompilationEngine engine;

        if(file.isDirectory()) {
            fileList = Arrays.asList(file.listFiles(e -> e.toString().contains(".jack")));
        } else {
            if(file.toString().contains(".jack")) {
                fileList.add(file);
            }
        }
        
        for (File f : fileList) {
            try {
                jack = new JackTokenizer(f);
                outputName = f.toString().substring(0, f.toString().lastIndexOf('.')) + ".vm";
                out = new FileWriter(outputName);
                engine = new CompilationEngine(jack, out);
                jack.advance();
                engine.compileClass();
                out.close();
            } catch(IOException e) {
                e.printStackTrace();
                return;
            } catch(NullPointerException e) {
                e.printStackTrace();
                System.out.println("Can't create file");
                return;
            }
        }
    }
}
