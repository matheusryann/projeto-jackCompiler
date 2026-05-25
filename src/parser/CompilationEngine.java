package parser;

import lexer.Token;
import lexer.TokenType;
import symbol.SymbolTable;
import vm.VMWriter;

import java.util.List;

/**
 * Motor de compilação Jack → VM (Project 11).
 * Estrutura recursiva igual ao {@link Parser}; emite código via {@link VMWriter}.
 */
public class CompilationEngine {

    private final List<Token> tokens;
    private final VMWriter vmWriter;
    private final SymbolTable symbolTable = new SymbolTable();

    private int current;
    private String className;
    private String subroutineName;
    private String subroutineKind; // constructor | function | method
    private int labelCounter;

    public CompilationEngine(List<Token> tokens, VMWriter vmWriter) {
        this.tokens = tokens;
        this.vmWriter = vmWriter;
    }

    public void compileClass() {
        current = 0;
        labelCounter = 0;
        symbolTable.reset();

        match(TokenType.KEYWORD_CLASS);
        className = identifierLexeme();
        match(TokenType.IDENTIFIER);
        match(TokenType.LBRACE);

        while (peek() != null && isClassVarDecStart(peek().getType())) {
            compileClassVarDec();
        }

        while (peek() != null && isSubroutineDecStart(peek().getType())) {
            compileSubroutineDec();
        }

        match(TokenType.RBRACE);
        match(TokenType.EOF);
    }

    private void compileClassVarDec() {
        int kind = peek().getType() == TokenType.KEYWORD_STATIC
                ? SymbolTable.KIND_STATIC
                : SymbolTable.KIND_FIELD;
        match(peek().getType());

        String type = compileType();
        defineIdentifiers(type, kind);

        while (peek() != null && peek().getType() == TokenType.COMMA) {
            match(TokenType.COMMA);
            defineIdentifiers(type, kind);
        }

        match(TokenType.SEMICOLON);
    }

    private void compileSubroutineDec() {
        symbolTable.startSubroutine();

        Token kindToken = peek();
        if (kindToken.getType() == TokenType.KEYWORD_CONSTRUCTOR) {
            subroutineKind = "constructor";
        } else if (kindToken.getType() == TokenType.KEYWORD_FUNCTION) {
            subroutineKind = "function";
        } else {
            subroutineKind = "method";
        }
        match(kindToken.getType());

        if (peek() != null && peek().getType() == TokenType.KEYWORD_VOID) {
            match(TokenType.KEYWORD_VOID);
        } else {
            compileType();
        }

        subroutineName = identifierLexeme();
        match(TokenType.IDENTIFIER);
        match(TokenType.LPAREN);
        if ("method".equals(subroutineKind)) {
            symbolTable.reserveArgumentZeroForThis();
        }
        compileParameterList();
        match(TokenType.RPAREN);
        compileSubroutineBody();
    }

    private void compileParameterList() {
        if (peek() != null && isTypeStart(peek().getType())) {
            String type = compileType();
            symbolTable.define(identifierLexeme(), type, SymbolTable.KIND_ARG);
            match(TokenType.IDENTIFIER);

            while (peek() != null && peek().getType() == TokenType.COMMA) {
                match(TokenType.COMMA);
                type = compileType();
                symbolTable.define(identifierLexeme(), type, SymbolTable.KIND_ARG);
                match(TokenType.IDENTIFIER);
            }
        }
    }

    private void compileSubroutineBody() {
        match(TokenType.LBRACE);

        while (peek() != null && peek().getType() == TokenType.KEYWORD_VAR) {
            compileVarDec();
        }

        int nLocals = symbolTable.varCount(SymbolTable.KIND_VAR);
        vmWriter.writeFunction(functionName(), nLocals);
        compileSubroutineBootstrap();

        compileStatements();

        match(TokenType.RBRACE);
    }

    /**
     * Após {@code function}: inicializa {@code THIS} conforme tipo da subrotina (nand2tetris 11).
     */
    private void compileSubroutineBootstrap() {
        switch (subroutineKind) {
            case "method" -> {
                vmWriter.writePush(VMWriter.SEG_ARG, 0);
                vmWriter.writePop(VMWriter.SEG_POINTER, 0);
            }
            case "constructor" -> {
                int nFields = symbolTable.varCount(SymbolTable.KIND_FIELD);
                vmWriter.writePush(VMWriter.SEG_CONST, nFields);
                vmWriter.writeCall("Memory.alloc", 1);
                vmWriter.writePop(VMWriter.SEG_POINTER, 0);
            }
            case "function" -> { /* sem bootstrap */ }
            default -> throw new IllegalStateException("Tipo de subrotina: " + subroutineKind);
        }
    }

    /** Construtor Jack vira {@code Class.new} na VM. */
    private String functionName() {
        if ("constructor".equals(subroutineKind)) {
            return className + ".new";
        }
        return className + "." + subroutineName;
    }

    private void compileVarDec() {
        match(TokenType.KEYWORD_VAR);
        String type = compileType();
        symbolTable.define(identifierLexeme(), type, SymbolTable.KIND_VAR);
        match(TokenType.IDENTIFIER);

        while (peek() != null && peek().getType() == TokenType.COMMA) {
            match(TokenType.COMMA);
            symbolTable.define(identifierLexeme(), type, SymbolTable.KIND_VAR);
            match(TokenType.IDENTIFIER);
        }

        match(TokenType.SEMICOLON);
    }

    private void compileStatements() {
        while (peek() != null && isStatementStart(peek().getType())) {
            switch (peek().getType()) {
                case KEYWORD_LET -> compileLet();
                case KEYWORD_IF -> compileIf();
                case KEYWORD_WHILE -> compileWhile();
                case KEYWORD_DO -> compileDo();
                case KEYWORD_RETURN -> compileReturn();
                default -> throw syntaxError("statement");
            }
        }
    }

    private void compileLet() {
        match(TokenType.KEYWORD_LET);
        String name = identifierLexeme();
        match(TokenType.IDENTIFIER);

        if (peek() != null && peek().getType() == TokenType.LBRACKET) {
            compileVariablePush(name);
            match(TokenType.LBRACKET);
            compileExpression();
            vmWriter.writeArithmetic("add");
            vmWriter.writePop(VMWriter.SEG_POINTER, 1);
            match(TokenType.RBRACKET);
            match(TokenType.EQ);
            compileExpression();
            vmWriter.writePop(VMWriter.SEG_TEMP, 0);
            vmWriter.writePop(VMWriter.SEG_THAT, 0);
            vmWriter.writePush(VMWriter.SEG_TEMP, 0);
            vmWriter.writePop(VMWriter.SEG_THAT, 1);
        } else {
            match(TokenType.EQ);
            compileExpression();
            compileVariablePop(name);
        }

        match(TokenType.SEMICOLON);
    }

    private void compileIf() {
        String labelFalse = newLabel("IF_FALSE");
        String labelEnd = newLabel("IF_END");

        match(TokenType.KEYWORD_IF);
        match(TokenType.LPAREN);
        compileExpression();
        match(TokenType.RPAREN);
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(labelFalse);

        match(TokenType.LBRACE);
        compileStatements();
        match(TokenType.RBRACE);

        vmWriter.writeGoto(labelEnd);
        vmWriter.writeLabel(labelFalse);

        if (peek() != null && peek().getType() == TokenType.KEYWORD_ELSE) {
            match(TokenType.KEYWORD_ELSE);
            match(TokenType.LBRACE);
            compileStatements();
            match(TokenType.RBRACE);
        }

        vmWriter.writeLabel(labelEnd);
    }

    private void compileWhile() {
        String labelTop = newLabel("WHILE_TOP");
        String labelEnd = newLabel("WHILE_END");

        vmWriter.writeLabel(labelTop);

        match(TokenType.KEYWORD_WHILE);
        match(TokenType.LPAREN);
        compileExpression();
        match(TokenType.RPAREN);
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(labelEnd);

        match(TokenType.LBRACE);
        compileStatements();
        match(TokenType.RBRACE);

        vmWriter.writeGoto(labelTop);
        vmWriter.writeLabel(labelEnd);
    }

    private void compileDo() {
        match(TokenType.KEYWORD_DO);
        compileSubroutineCall();
        match(TokenType.SEMICOLON);
        vmWriter.writePop(VMWriter.SEG_TEMP, 0);
    }

    private void compileReturn() {
        match(TokenType.KEYWORD_RETURN);

        if (peek() != null && peek().getType() != TokenType.SEMICOLON) {
            compileExpression();
        } else {
            vmWriter.writePush(VMWriter.SEG_CONST, 0);
        }

        match(TokenType.SEMICOLON);
        vmWriter.writeReturn();
    }

    private String compileType() {
        Token t = peek();
        if (t == null) {
            throw syntaxError("type");
        }
        match(t.getType());
        return t.getLexeme();
    }

    private void compileExpression() {
        compileTerm();
        while (peek() != null && isBinaryOp(peek().getType())) {
            String op = peek().getLexeme();
            advance();
            compileTerm();
            writeBinaryOp(op);
        }
    }

    private void compileTerm() {
        Token t = peek();
        if (t == null) {
            throw syntaxError("term");
        }

        switch (t.getType()) {
            case LPAREN -> {
                match(TokenType.LPAREN);
                compileExpression();
                match(TokenType.RPAREN);
            }
            case MINUS -> {
                advance();
                compileTerm();
                vmWriter.writeArithmetic("neg");
            }
            case TILDE -> {
                advance();
                compileTerm();
                vmWriter.writeArithmetic("not");
            }
            case INTEGER_CONSTANT -> {
                advance();
                vmWriter.writePush(VMWriter.SEG_CONST, Integer.parseInt(t.getLexeme()));
            }
            case STRING_CONSTANT -> {
                advance();
                // passo 2.10
            }
            case KEYWORD_TRUE -> {
                advance();
                vmWriter.writePush(VMWriter.SEG_CONST, -1);
            }
            case KEYWORD_FALSE, KEYWORD_NULL -> {
                advance();
                vmWriter.writePush(VMWriter.SEG_CONST, 0);
            }
            case KEYWORD_THIS -> {
                advance();
                vmWriter.writePush(VMWriter.SEG_POINTER, 0);
            }
            case IDENTIFIER -> {
                String name = t.getLexeme();
                if (peekAhead(1) != null && peekAhead(1).getType() == TokenType.LBRACKET) {
                    compileVariablePush(name);
                    match(TokenType.IDENTIFIER);
                    match(TokenType.LBRACKET);
                    compileExpression();
                    match(TokenType.RBRACKET);
                    vmWriter.writeArithmetic("add");
                    vmWriter.writePop(VMWriter.SEG_POINTER, 1);
                    vmWriter.writePush(VMWriter.SEG_THAT, 0);
                } else if (peekAhead(1) != null
                        && (peekAhead(1).getType() == TokenType.LPAREN
                        || peekAhead(1).getType() == TokenType.DOT)) {
                    compileSubroutineCall();
                } else {
                    compileVariablePush(name);
                    match(TokenType.IDENTIFIER);
                }
            }
            default -> throw syntaxError("term: " + t.getLexeme());
        }
    }

    private void compileSubroutineCall() {
        String callName = identifierLexeme();
        match(TokenType.IDENTIFIER);

        if (peek() != null && peek().getType() == TokenType.DOT) {
            match(TokenType.DOT);
            callName = callName + "." + identifierLexeme();
            match(TokenType.IDENTIFIER);
        } else {
            callName = className + "." + callName;
        }

        match(TokenType.LPAREN);
        int nArgs = compileExpressionList();
        match(TokenType.RPAREN);

        vmWriter.writeCall(callName, nArgs);
    }

    private int compileExpressionList() {
        int nArgs = 0;
        if (peek() != null && peek().getType() != TokenType.RPAREN) {
            compileExpression();
            nArgs++;
            while (peek() != null && peek().getType() == TokenType.COMMA) {
                match(TokenType.COMMA);
                compileExpression();
                nArgs++;
            }
        }
        return nArgs;
    }

    private void compileVariablePush(String name) {
        int kind = symbolTable.kindOf(name);
        vmWriter.writePush(segmentFor(kind), symbolTable.indexOf(name));
    }

    private void compileVariablePop(String name) {
        int kind = symbolTable.kindOf(name);
        vmWriter.writePop(segmentFor(kind), symbolTable.indexOf(name));
    }

    private static String segmentFor(int kind) {
        return switch (kind) {
            case SymbolTable.KIND_STATIC -> VMWriter.SEG_STATIC;
            case SymbolTable.KIND_FIELD -> VMWriter.SEG_THIS;
            case SymbolTable.KIND_ARG -> VMWriter.SEG_ARG;
            case SymbolTable.KIND_VAR -> VMWriter.SEG_LOCAL;
            default -> throw new IllegalArgumentException("kind: " + kind);
        };
    }

    private void writeBinaryOp(String op) {
        switch (op) {
            case "+" -> vmWriter.writeArithmetic("add");
            case "-" -> vmWriter.writeArithmetic("sub");
            case "*" -> vmWriter.writeCall("Math.multiply", 2);
            case "/" -> vmWriter.writeCall("Math.divide", 2);
            case "&" -> vmWriter.writeArithmetic("and");
            case "|" -> vmWriter.writeArithmetic("or");
            case "<" -> vmWriter.writeArithmetic("lt");
            case ">" -> vmWriter.writeArithmetic("gt");
            case "=" -> vmWriter.writeArithmetic("eq");
            default -> throw new IllegalStateException("Operador inválido: " + op);
        }
    }

    private void defineIdentifiers(String type, int kind) {
        symbolTable.define(identifierLexeme(), type, kind);
        match(TokenType.IDENTIFIER);
    }

    private String identifierLexeme() {
        return peek().getLexeme();
    }

    private String newLabel(String prefix) {
        return prefix + labelCounter++;
    }

    private static boolean isBinaryOp(TokenType type) {
        return switch (type) {
            case PLUS, MINUS, STAR, SLASH, AND, PIPE, LT, GT, EQ -> true;
            default -> false;
        };
    }

    private static boolean isClassVarDecStart(TokenType type) {
        return type == TokenType.KEYWORD_STATIC || type == TokenType.KEYWORD_FIELD;
    }

    private static boolean isSubroutineDecStart(TokenType type) {
        return type == TokenType.KEYWORD_CONSTRUCTOR
                || type == TokenType.KEYWORD_FUNCTION
                || type == TokenType.KEYWORD_METHOD;
    }

    private static boolean isTypeStart(TokenType type) {
        return type == TokenType.KEYWORD_INT
                || type == TokenType.KEYWORD_CHAR
                || type == TokenType.KEYWORD_BOOLEAN
                || type == TokenType.IDENTIFIER;
    }

    private static boolean isStatementStart(TokenType type) {
        return type == TokenType.KEYWORD_LET
                || type == TokenType.KEYWORD_IF
                || type == TokenType.KEYWORD_WHILE
                || type == TokenType.KEYWORD_DO
                || type == TokenType.KEYWORD_RETURN;
    }

    private Token peek() {
        if (current >= tokens.size()) {
            return null;
        }
        return tokens.get(current);
    }

    private Token peekAhead(int offset) {
        int i = current + offset;
        if (i >= tokens.size()) {
            return null;
        }
        return tokens.get(i);
    }

    private void advance() {
        if (current < tokens.size()) {
            current++;
        }
    }

    private void match(TokenType expected) {
        Token token = peek();
        if (token == null || token.getType() != expected) {
            String got = token == null ? "EOF" : token.getType().name();
            throw new IllegalStateException(
                    "Esperado " + expected + ", encontrado " + got
                            + (token != null ? " (linha " + token.getLine() + ")" : ""));
        }
        advance();
    }

    private IllegalStateException syntaxError(String what) {
        Token t = peek();
        String at = t == null ? "EOF" : t.getLexeme() + " linha " + t.getLine();
        return new IllegalStateException("Erro sintático em " + what + ": " + at);
    }
}
