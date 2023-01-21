public class Symbol {

    private String type;
    private String kind;
    private int index;

    public Symbol(String type, String kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public String getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }

    public void setType(String val) {
        type = val;
    }

    public void setKind(String val) {
        kind = val;
    }

    public void setIndex(int val) {
        index = val;
    }
}