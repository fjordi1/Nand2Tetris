import java.util.HashMap;

public class SymbolTable {
    private HashMap<String,Symbol> classSym;    // STATIC, FIELD
    private HashMap<String,Symbol> subroutineSym;   // ARG, VAR

    private int [] index;
    

    public SymbolTable() {
        classSym = new HashMap<String, Symbol>();
        subroutineSym = new HashMap<String, Symbol>();

        index = new int [4]; // index[0] = STATIC, index[1] = FIELD, index[2] = ARG, index[3] = VAR
    }

    public void reset() {
        subroutineSym.clear();
        index[2] = 0; // arg index -> 0
        index[3] = 0; // var index -> 0
    }

    public void define(String name, String type, String kind) {
        if (classSym.containsKey(name) || subroutineSym.containsKey(name) ){
            return;
        }
        else{
            if(kind.equals("STATIC") || kind.equals("static")){
                Symbol sym = new Symbol(type,kind,index[0]);
                index[0]++;
                classSym.put(name, sym);
            }
            else if(kind.equals("FIELD") || kind.equals("field")){
                Symbol sym = new Symbol(type,kind,index[1]);
                index[1]++;
                classSym.put(name, sym);
            }
            else if (kind.equals("ARG") || kind.equals("argument")){
                Symbol sym = new Symbol(type,kind,index[2]);
                index[2]++;
                subroutineSym.put(name, sym);
            }
            else if(kind.equals("VAR") || kind.equals("local")){
                Symbol sym = new Symbol(type,kind,index[3]);
                index[3]++;
                subroutineSym.put(name, sym);
            }
            return;
        }
    }

    public int varCount(String kind) {
        if(kind.equals("STATIC")){
            return index[0];
        }
        else if(kind.equals("FIELD")){
            return index[1];
        }
        else if(kind.equals("ARG")){
            return index[2];
        }
        else { 
            return index[3];
            }
        }

    public String kindOf(String name) {
        if (classSym.get(name) != null){ //checks if 'name' is in the hashtable "Class"
            return classSym.get(name).getKind();
        }else if (subroutineSym.get(name) != null){ //checks if 'name' is in the hashtable "Subroutine"
            return subroutineSym.get(name).getKind();
        } else
        return null;
    }

    public String typeOf(String name) {
        if (classSym.get(name) != null){ //checks if 'name' is in the hashtable "Class"
            return classSym.get(name).getType();
        }else if (subroutineSym.get(name) != null){ //checks if 'name' is in the hashtable "Subroutine"
            return subroutineSym.get(name).getType();
        } else
        return "";
    }
    
    public int indexOf(String name) {
        if (classSym.get(name) != null){ //checks if 'name' is in the hashtable "Class"
            return classSym.get(name).getIndex();
        }else if (subroutineSym.get(name) != null){ //checks if 'name' is in the hashtable "Subroutine"
            return subroutineSym.get(name).getIndex();
        } else
        return -1;
    }
}
