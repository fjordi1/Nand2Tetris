import java.util.HashMap;

public class SymbolTable {
    private HashMap<String,Symbol> classSym;    // STATIC, FIELD
    private HashMap<String,Symbol> subroutineSym;   // ARG, VAR

    private int [] index;
    

    public SymbolTable() {
        classSym = new HashMap<String, Symbol>();
        subroutineSym = new HashMap<String, Symbol>();

        index = new int [4]; // 0 = STATIC, 1 = FIELD, 2 = ARG, 3 = VAR
    }

    public void reset() {

    }

    public void define(String name, String type, String kind) {

    }

    public int varCount(String kind) {

    }

    public String kindOf(String name) {

    }

    public String typeOf(String name) {

    }
    
    public int indexOf(String name) {

    }
}
