package jackcompiler.parser;

import jackcompiler.lexer.Token;
import jackcompiler.lexer.TokenType;

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

    /**
     * Gera o XML do programa (por agora: uma única classe vazia {@code class Name { }}).
     */
    public String parse() {
        xmlLines.clear();
        current = 0;
        indentLevel = 0;
        compileClass();
        return getXml();
    }

    /**
     * Interpreta um único termo e devolve o XML (útil em testes e até integrar em {@code expression}).
     */
    public String parseTerm() {
        xmlLines.clear();
        current = 0;
        indentLevel = 0;
        compileTerm();
        return getXml();
    }

    /**
     * Interpreta uma expressão completa e devolve o XML (útil em testes).
     */
    public String parseExpression() {
        xmlLines.clear();
        current = 0;
        indentLevel = 0;
        compileExpression();
        return getXml();
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

    private void compileClass() {
        openTag("class");
        match(TokenType.KEYWORD_CLASS);
        match(TokenType.IDENTIFIER);
        match(TokenType.LBRACE);
        // classVarDec* e subroutineDec* — incrementos futuros
        match(TokenType.RBRACE);
        match(TokenType.EOF);
        closeTag("class");
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
