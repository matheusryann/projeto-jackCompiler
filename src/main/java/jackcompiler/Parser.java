package jackcompiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Analisador sintático descendente recursivo: consome {@link Token}s e emite XML
 * da árvore de parsing (Nand2Tetris Project 10).
 * 
 */
public class Parser {

    private final List<Token> tokens;
    private int current;  //lookahead, aponta para o proximo token a ser consumido
    private final List<String> xmlLines = new ArrayList<>();
    private int indentLevel; //controla o nível de indentação do XML

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
     * term → integerConstant | stringConstant | keywordConstant | varName | … (chamadas e indexação depois)
     */
    private void compileTerm() {
        openTag("term");
        Token t = peek();
        if (t == null) {
            throw new IllegalStateException("Termo esperado, encontrado fim da entrada");
        }
        switch (t.getType()) {
            case INTEGER_CONSTANT:
            case STRING_CONSTANT:
            case KEYWORD_TRUE:
            case KEYWORD_FALSE:
            case KEYWORD_NULL:
            case KEYWORD_THIS:
            case IDENTIFIER:
                writeToken(t);
                advance();
                break;
            default:
                throw new IllegalStateException(
                        "Termo esperado, encontrado: " + t.getLexeme() + " (linha " + t.getLine() + ")");
        }
        closeTag("term");
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

    private Token advance() {
        Token t = peek();
        if (t != null) {
            current++;
        }
        return t;
    }

    private void match(TokenType expected) {
        Token token = peek();

        // Se o token não for o esperado ou for EOF, lança uma exceção
        if (token == null || token.getType() != expected) {
            String got = token == null ? "EOF" : String.valueOf(token.getType());
            throw new IllegalStateException(
                    "Esperado " + expected + ", encontrado " + got
                            + (token != null ? " (linha " + token.getLine() + ")" : ""));
        }

        // Se for o esperado não for EOF, escreve o token no XML
        // e avança para o próximo token
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
