package parser;

import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Analisador sintático descendente recursivo: consome {@link Token}s e emite XML
 * da árvore de parsing (Nand2Tetris Project 10).
 */
public class Parser {

    private final List<Token> tokens;
    private int current;  // lookahead, aponta para o próximo token a ser consumido
    private final List<String> xmlLines = new ArrayList<>();
    private int indentLevel; // controla o nível de indentação do XML

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public String parse() {
        xmlLines.clear();
        current = 0;
        indentLevel = 0;
        compileClass();
        return getXml();
    }

    public String parseTerm() {
        xmlLines.clear();
        current = 0;
        indentLevel = 0;
        compileTerm();
        return getXml();
    }

    public String parseExpression() {
        xmlLines.clear();
        current = 0;
        indentLevel = 0;
        compileExpression();
        return getXml();
    }

    /**
     * class → 'class' className '{' classVarDec* subroutineDec* '}'
     */
    private void compileClass() {
        openTag("class");
        match(TokenType.KEYWORD_CLASS);
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
        closeTag("class");
    }

    /**
     * classVarDec → ('static' | 'field') type varName (',' varName)* ';'
     */
    private void compileClassVarDec() {
        openTag("classVarDec");

        if (peek() != null && peek().getType() == TokenType.KEYWORD_STATIC) {
            match(TokenType.KEYWORD_STATIC);
        } else {
            match(TokenType.KEYWORD_FIELD);
        }

        compileType();
        match(TokenType.IDENTIFIER);

        while (peek() != null && peek().getType() == TokenType.COMMA) {
            match(TokenType.COMMA);
            match(TokenType.IDENTIFIER);
        }

        match(TokenType.SEMICOLON);
        closeTag("classVarDec");
    }

    /**
     * subroutineDec → ('constructor' | 'function' | 'method')
     *                 ('void' | type) subroutineName '(' parameterList ')' subroutineBody
     */
    private void compileSubroutineDec() {
        openTag("subroutineDec");

        if (peek() != null && peek().getType() == TokenType.KEYWORD_CONSTRUCTOR) {
            match(TokenType.KEYWORD_CONSTRUCTOR);
        } else if (peek() != null && peek().getType() == TokenType.KEYWORD_FUNCTION) {
            match(TokenType.KEYWORD_FUNCTION);
        } else {
            match(TokenType.KEYWORD_METHOD);
        }

        if (peek() != null && peek().getType() == TokenType.KEYWORD_VOID) {
            match(TokenType.KEYWORD_VOID);
        } else {
            compileType();
        }

        match(TokenType.IDENTIFIER);
        match(TokenType.LPAREN);
        compileParameterList();
        match(TokenType.RPAREN);
        compileSubroutineBody();

        closeTag("subroutineDec");
    }

    /**
     * parameterList → ((type varName) (',' type varName)*)?
     */
    private void compileParameterList() {
        openTag("parameterList");

        if (peek() != null && isTypeStart(peek().getType())) {
            compileType();
            match(TokenType.IDENTIFIER);

            while (peek() != null && peek().getType() == TokenType.COMMA) {
                match(TokenType.COMMA);
                compileType();
                match(TokenType.IDENTIFIER);
            }
        }

        closeTag("parameterList");
    }

    /**
     * subroutineBody → '{' varDec* statements '}'
     */
    private void compileSubroutineBody() {
        openTag("subroutineBody");
        match(TokenType.LBRACE);

        while (peek() != null && peek().getType() == TokenType.KEYWORD_VAR) {
            compileVarDec();
        }

        compileStatements();

        match(TokenType.RBRACE);
        closeTag("subroutineBody");
    }

    /**
     * varDec → 'var' type varName (',' varName)* ';'
     */
    private void compileVarDec() {
        openTag("varDec");
        match(TokenType.KEYWORD_VAR);
        compileType();
        match(TokenType.IDENTIFIER);

        while (peek() != null && peek().getType() == TokenType.COMMA) {
            match(TokenType.COMMA);
            match(TokenType.IDENTIFIER);
        }

        match(TokenType.SEMICOLON);
        closeTag("varDec");
    }

    /**
     * statements → statement*
     */
    private void compileStatements() {
        openTag("statements");

        while (peek() != null && isStatementStart(peek().getType())) {
            switch (peek().getType()) {
                case KEYWORD_LET -> compileLet();
                case KEYWORD_IF -> compileIf();
                case KEYWORD_WHILE -> compileWhile();
                case KEYWORD_DO -> compileDo();
                case KEYWORD_RETURN -> compileReturn();
                default -> throw new IllegalStateException(
                        "Statement inesperado: " + peek().getLexeme() + " (linha " + peek().getLine() + ")");
            }
        }

        closeTag("statements");
    }

    /**
     * letStatement → 'let' varName ('[' expression ']')? '=' expression ';'
     */
    private void compileLet() {
        openTag("letStatement");

        match(TokenType.KEYWORD_LET);
        match(TokenType.IDENTIFIER);

        if (peek() != null && peek().getType() == TokenType.LBRACKET) {
            match(TokenType.LBRACKET);
            compileExpression();
            match(TokenType.RBRACKET);
        }

        match(TokenType.EQ);
        compileExpression();
        match(TokenType.SEMICOLON);

        closeTag("letStatement");
    }

    /**
     * ifStatement → 'if' '(' expression ')' '{' statements '}'
     *               ('else' '{' statements '}')?
     */
    private void compileIf() {
        openTag("ifStatement");

        match(TokenType.KEYWORD_IF);
        match(TokenType.LPAREN);
        compileExpression();
        match(TokenType.RPAREN);
        match(TokenType.LBRACE);
        compileStatements();
        match(TokenType.RBRACE);

        if (peek() != null && peek().getType() == TokenType.KEYWORD_ELSE) {
            match(TokenType.KEYWORD_ELSE);
            match(TokenType.LBRACE);
            compileStatements();
            match(TokenType.RBRACE);
        }

        closeTag("ifStatement");
    }

    /**
     * whileStatement → 'while' '(' expression ')' '{' statements '}'
     */
    private void compileWhile() {
        openTag("whileStatement");

        match(TokenType.KEYWORD_WHILE);
        match(TokenType.LPAREN);
        compileExpression();
        match(TokenType.RPAREN);
        match(TokenType.LBRACE);
        compileStatements();
        match(TokenType.RBRACE);

        closeTag("whileStatement");
    }

    /**
     * doStatement → 'do' subroutineCall ';'
     */
    private void compileDo() {
        openTag("doStatement");

        match(TokenType.KEYWORD_DO);
        compileSubroutineCall();
        match(TokenType.SEMICOLON);

        closeTag("doStatement");
    }

    /**
     * returnStatement → 'return' expression? ';'
     */
    private void compileReturn() {
        openTag("returnStatement");

        match(TokenType.KEYWORD_RETURN);

        if (peek() != null && peek().getType() != TokenType.SEMICOLON) {
            compileExpression();
        }

        match(TokenType.SEMICOLON);
        closeTag("returnStatement");
    }

    /**
     * type → 'int' | 'char' | 'boolean' | className
     */
    private void compileType() {
        Token t = peek();
        if (t == null) {
            throw new IllegalStateException("Tipo esperado, encontrado EOF");
        }

        switch (t.getType()) {
            case KEYWORD_INT -> match(TokenType.KEYWORD_INT);
            case KEYWORD_CHAR -> match(TokenType.KEYWORD_CHAR);
            case KEYWORD_BOOLEAN -> match(TokenType.KEYWORD_BOOLEAN);
            case IDENTIFIER -> match(TokenType.IDENTIFIER);
            default -> throw new IllegalStateException(
                    "Tipo esperado, encontrado: " + t.getLexeme() + " (linha " + t.getLine() + ")");
        }
    }

    /**
     * term → integerConstant | stringConstant | keywordConstant |
     *        varName | varName '[' expression ']' | subroutineCall |
     *        '(' expression ')' | unaryOp term
     */
    private void compileTerm() {
        openTag("term");
        Token t = peek();
        if (t == null) {
            throw new IllegalStateException("Termo esperado, encontrado fim da entrada");
        }
        switch (t.getType()) {
            case LPAREN -> {
                match(TokenType.LPAREN);
                compileExpression();
                match(TokenType.RPAREN);
            }
            case MINUS, TILDE -> {
                writeToken(t);
                advance();
                compileTerm();
            }
            case INTEGER_CONSTANT, STRING_CONSTANT, KEYWORD_TRUE, KEYWORD_FALSE, KEYWORD_NULL, KEYWORD_THIS -> {
                writeToken(t);
                advance();
            }
            case IDENTIFIER -> {
                Token next = peekAhead(1);
                if (next != null && next.getType() == TokenType.LBRACKET) {
                    match(TokenType.IDENTIFIER);
                    match(TokenType.LBRACKET);
                    compileExpression();
                    match(TokenType.RBRACKET);
                } else if (next != null
                        && (next.getType() == TokenType.LPAREN || next.getType() == TokenType.DOT)) {
                    compileSubroutineCall();
                } else {
                    match(TokenType.IDENTIFIER);
                }
            }
            default -> throw new IllegalStateException(
                    "Termo esperado, encontrado: " + t.getLexeme() + " (linha " + t.getLine() + ")");
        }
        closeTag("term");
    }

    /**
     * subroutineCall → subroutineName '(' expressionList ')' |
     *                  (className | varName) '.' subroutineName '(' expressionList ')'
     */
    private void compileSubroutineCall() {
        match(TokenType.IDENTIFIER);
        if (peek() != null && peek().getType() == TokenType.DOT) {
            match(TokenType.DOT);
            match(TokenType.IDENTIFIER);
        }
        match(TokenType.LPAREN);
        compileExpressionList();
        match(TokenType.RPAREN);
    }

    /**
     * expressionList → (expression (',' expression)*)?
     */
    private void compileExpressionList() {
        openTag("expressionList");
        if (peek() != null && peek().getType() != TokenType.RPAREN) {
            compileExpression();
            while (peek() != null && peek().getType() == TokenType.COMMA) {
                match(TokenType.COMMA);
                compileExpression();
            }
        }
        closeTag("expressionList");
    }

    /**
     * expression → term (op term)*
     * No XML do Project 10, cada {@code op} é um {@code symbol} (+ - * / &amp; | &lt; &gt; =).
     */
    private void compileExpression() {
        openTag("expression");
        compileTerm();
        while (peek() != null && isBinaryOp(peek().getType())) {
            writeToken(peek());
            advance();
            compileTerm();
        }
        closeTag("expression");
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

    /** Próximo token após {@code peek()}, sem consumir (offset 1 = segundo token à frente). */
    private Token peekAhead(int offset) {
        int i = current + offset;
        if (i >= tokens.size()) {
            return null;
        }
        return tokens.get(i);
    }

    private Token advance() {
        Token t = peek();
        if (t != null) {
            current++;
        }
        return t;
    }

    private void match(TokenType expected) {
        Token token = peek();

        if (token == null || token.getType() != expected) {
            String got = token == null ? "EOF" : String.valueOf(token.getType());
            throw new IllegalStateException(
                    "Esperado " + expected + ", encontrado " + got
                            + (token != null ? " (linha " + token.getLine() + ")" : ""));
        }

        if (expected != TokenType.EOF) {
            writeToken(token);
        }
        advance();
    }

    private void openTag(String name) {
        xmlLines.add(indent() + "<" + name + ">");
        indentLevel++;
    }

    private void closeTag(String name) {
        indentLevel--;
        xmlLines.add(indent() + "</" + name + ">");
    }

    private void writeToken(Token token) {
        xmlLines.add(indent() + token.toXml());
    }

    private String indent() {
        return "  ".repeat(indentLevel);
    }

    private String getXml() {
        return String.join("\n", xmlLines);
    }
}