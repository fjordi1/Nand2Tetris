import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;
import java.util.List;

public class JackTokenizer {
    private Scanner scan;
    public String token;

    private String [] keywordsTemp = {"class", "constructor", "function", "method", "field", "static", "var", "int", "char" , "boolean",
    "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return"};
    private List<String> keywords = Arrays.asList(keywordsTemp);

    private String [] symbolsTemp = {"{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~"};
    private List<String> symbols = Arrays.asList(symbolsTemp);

    private String [] opTemp = {"+", "-", "*", "/", "&", "|", "<", ">", "="};
    private List<String> op = Arrays.asList(opTemp);

    private Stack<String> stack = new Stack<String>();

    public JackTokenizer(File file) {
        try {
            this.scan = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean hasMoreTokens() {
        return scan.hasNext() || !stack.isEmpty();
    }

    // Only called if hasMoreTokens is true
    public void advance() {
        // We define a stack of strings to be able to seperate expressions into different tokens
        String tkn;
        if(stack.isEmpty()) {
            tkn = scan.next();
        } else {
            tkn = stack.pop();
        }
        if(tkn.charAt(0) == '"') {
            token = tkn;
            tkn = scan.next();
            while(tkn.indexOf('"') == -1) {
                token += " " + tkn;
                tkn = scan.next();
            }
            String[] split = tkn.split("\"");
            token += split[0] + '"';
            stack.push(split[1]);
            return;
        } 
        if(tkn.length() == 1) {
            token = tkn;
            return;
        }
        if(tkn.contains("//") || tkn.isEmpty()) {
            scan.nextLine();
            advance();
            return;
        }
        if(tkn.contains("/**")) {
            tkn = scan.nextLine();
            while(!tkn.contains("*/")) {
                tkn = scan.nextLine();
            }
            advance();
            return;
        }
        for (String symbol : symbols) {
            int i = tkn.indexOf(symbol);
            if(i == 0) {
                stack.push(tkn.substring(i+1));
                stack.push("" + tkn.charAt(0));
                advance();
                return;
            } else if(i != -1) {
                stack.push(tkn.substring(i));
                stack.push(tkn.substring(0, i));
                advance();
                return;
            }
        }
        token = tkn;
    }

    public String tokenType() {
        if(token.charAt(0) == '"') {
            return "stringConstant";
        }
        if(Character.isDigit(token.charAt(0))) {
            return "integerConstant";
        }
        if(symbols.contains(token)) {
            return "symbol";
        }
        if(keywords.contains(token)) {
            return "keyword";
        }
        else {
            return "identifier";
        }
    }

    public String keyWord() {
        return token.toUpperCase();
    }

    public char symbol() {
        return token.charAt(0);
    }

    public String identifier() {
        return token;
    }

    public int intVal() {
        return Integer.parseInt(token);
    }

    public String stringVal() {
        return token.substring(1, token.length()-1);
    }

    // Checks if token is an operator
    public Boolean isOp() {
        return op.contains(token);
    }
}
