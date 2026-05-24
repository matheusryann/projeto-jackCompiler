package parser;

import lexer.Token;
import lexer.TokenType;
import symbol.SymbolTable;
import vm.VMWriter;

import java.util.List;

public class CompilationEngine {
    private final List<Token> tokens;
    private int current = 0;
    private final VMWriter vmWriter;
    private final SymbolTable symbolTable;
    
    private String currentClassName;
    private int labelCounter = 0;

    public CompilationEngine(List<Token> tokens, VMWriter vmWriter) {
        this.tokens = tokens;
        this.vmWriter = vmWriter;
        this.symbolTable = new SymbolTable();
    }

    private Token peek() { return tokens.get(current); }
    private Token advance() { return tokens.get(current++); }
    private boolean isAtEnd() { return current >= tokens.size() || peek().getType() == TokenType.EOF; }

    public void compileClass() {
        advance(); 
        currentClassName = advance().getLexeme(); 
        advance(); 
   
        while (!isAtEnd() && (peek().getLexeme().equals("static") || peek().getLexeme().equals("field"))) {
            
            advance(); 
        }

        while (!isAtEnd() && (peek().getLexeme().equals("constructor") || peek().getLexeme().equals("function") || peek().getLexeme().equals("method"))) {
            compileSubroutine();
        }
        advance(); 
    }

    private void compileSubroutine() {
        symbolTable.startSubroutine(); 
        
        String subroutineType = advance().getLexeme(); 
        advance(); 
        String subroutineName = advance().getLexeme(); 
        
        advance(); 
        while (!peek().getLexeme().equals(")")) { advance(); }
        advance(); 

        advance(); 
        
        int nLocals = 0;
        while (peek().getLexeme().equals("var")) {
            advance();
        }

        vmWriter.writeFunction(currentClassName + "." + subroutineName, nLocals);

        compileStatements();
        
        advance(); 
    }

    private void compileStatements() {
        while (!isAtEnd() && !peek().getLexeme().equals("}")) {
            String keyword = peek().getLexeme();
            switch (keyword) {
                case "do": compileDo(); break;
                case "return": compileReturn(); break;
                default: advance(); break; 
            }
        }
    }

    private void compileDo() {
        advance(); 
        
        String className = advance().getLexeme(); 
        advance(); 
        String funcName = advance().getLexeme(); 
        
        advance(); 
        int nArgs = compileExpressionList(); 
        advance(); 
        advance(); 
        
        vmWriter.writeCall(className + "." + funcName, nArgs);
        vmWriter.writePop("temp", 0); 
    }

    private void compileReturn() {
        advance(); 
        if (!peek().getLexeme().equals(";")) {
            compileExpression();
        } else {
            vmWriter.writePush("constant", 0); 
        }
        advance(); 
        vmWriter.writeReturn();
    }

    private int compileExpressionList() {
        int nArgs = 0;
        if (!peek().getLexeme().equals(")")) {
            compileExpression();
            nArgs++;
            while (peek().getLexeme().equals(",")) {
                advance(); 
                compileExpression();
                nArgs++;
            }
        }
        return nArgs;
    }

    private void compileExpression() {
        compileTerm();
        while (isOperator(peek().getLexeme())) {
            String op = advance().getLexeme();
            compileTerm();
            writeArithmetic(op); 
        }
    }

    private void compileTerm() {
        Token token = advance();
        
        if (token.getType() == TokenType.INTEGER_CONSTANT) { 
            vmWriter.writePush("constant", Integer.parseInt(token.getLexeme()));
        } 
        else if (token.getLexeme().equals("(")) {
            compileExpression();
            advance(); 
        }
        // ... (Construiremos variáveis e chamadas de métodos aqui depois)
    }

    private void writeArithmetic(String op) {
        switch (op) {
            case "+": vmWriter.writeArithmetic("add"); break;
            case "-": vmWriter.writeArithmetic("sub"); break;
            case "*": vmWriter.writeCall("Math.multiply", 2); break;
            case "/": vmWriter.writeCall("Math.divide", 2); break;
            case "&": vmWriter.writeArithmetic("and"); break;
            case "|": vmWriter.writeArithmetic("or"); break;
            case "<": vmWriter.writeArithmetic("lt"); break;
            case ">": vmWriter.writeArithmetic("gt"); break;
            case "=": vmWriter.writeArithmetic("eq"); break;
        }
    }

    private boolean isOperator(String lexeme) {
        return "+-*/&|<>=".contains(lexeme);
    }
}