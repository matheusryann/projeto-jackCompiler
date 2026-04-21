package jackcompiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int current = 0;
    private int line = 1;

    private static final Map<Character, TokenType> SYMBOLS = new HashMap<>();
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        SYMBOLS.put('{', TokenType.LBRACE);
        SYMBOLS.put('}', TokenType.RBRACE);
        SYMBOLS.put('(', TokenType.LPAREN);
        SYMBOLS.put(')', TokenType.RPAREN);
        SYMBOLS.put('[', TokenType.LBRACKET);
        SYMBOLS.put(']', TokenType.RBRACKET);
        SYMBOLS.put('.', TokenType.DOT);
        SYMBOLS.put(',', TokenType.COMMA);
        SYMBOLS.put(';', TokenType.SEMICOLON);
        SYMBOLS.put('+', TokenType.PLUS);
        SYMBOLS.put('-', TokenType.MINUS);
        SYMBOLS.put('*', TokenType.STAR);
        SYMBOLS.put('/', TokenType.SLASH);
        SYMBOLS.put('&', TokenType.AND);
        SYMBOLS.put('|', TokenType.PIPE);
        SYMBOLS.put('<', TokenType.LT);
        SYMBOLS.put('>', TokenType.GT);
        SYMBOLS.put('=', TokenType.EQ);
        SYMBOLS.put('~', TokenType.TILDE);

        KEYWORDS.put("class", TokenType.KEYWORD_CLASS);
        KEYWORDS.put("constructor", TokenType.KEYWORD_CONSTRUCTOR);
        KEYWORDS.put("function", TokenType.KEYWORD_FUNCTION);
        KEYWORDS.put("method", TokenType.KEYWORD_METHOD);
        KEYWORDS.put("field", TokenType.KEYWORD_FIELD);
        KEYWORDS.put("static", TokenType.KEYWORD_STATIC);
        KEYWORDS.put("var", TokenType.KEYWORD_VAR);
        KEYWORDS.put("int", TokenType.KEYWORD_INT);
        KEYWORDS.put("char", TokenType.KEYWORD_CHAR);
        KEYWORDS.put("boolean", TokenType.KEYWORD_BOOLEAN);
        KEYWORDS.put("void", TokenType.KEYWORD_VOID);
        KEYWORDS.put("true", TokenType.KEYWORD_TRUE);
        KEYWORDS.put("false", TokenType.KEYWORD_FALSE);
        KEYWORDS.put("null", TokenType.KEYWORD_NULL);
        KEYWORDS.put("this", TokenType.KEYWORD_THIS);
        KEYWORDS.put("let", TokenType.KEYWORD_LET);
        KEYWORDS.put("do", TokenType.KEYWORD_DO);
        KEYWORDS.put("if", TokenType.KEYWORD_IF);
        KEYWORDS.put("else", TokenType.KEYWORD_ELSE);
        KEYWORDS.put("while", TokenType.KEYWORD_WHILE);
        KEYWORDS.put("return", TokenType.KEYWORD_RETURN);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            skipWhitespace();

            if (isAtEnd()) {
                break;
            }

            char ch = peek();

            if (ch == '/' && peek(1) == '/') {
                advance();
                advance();
                skipLineComment();
                continue;
            }

            if (ch == '/' && peek(1) == '*') {
                advance();
                advance();
                skipBlockComment();
                continue;
            }

            if (isIdentifierStart(ch)) {
                tokens.add(readIdentifier());
            } else if (Character.isDigit(ch)) {
                tokens.add(readNumber());
            } else if (ch == '"') {
                tokens.add(readString());
            } else if (SYMBOLS.containsKey(ch)) {
                tokens.add(readSymbol());
            } else {
                throw new RuntimeException("Caractere ilegal '" + ch + "' na linha " + line);
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private Token readNumber() {
        int start = current;

        while (Character.isDigit(peek())) {
            advance();
        }

        return new Token(TokenType.INTEGER_CONSTANT, source.substring(start, current), line);
    }

    private Token readString() {
        advance();
        int start = current;

        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') {
                throw new RuntimeException("String não pode quebrar linha. Erro na linha " + line);
            }
            advance();
        }

        if (isAtEnd()) {
            throw new RuntimeException("String não fechada na linha " + line);
        }

        String lexeme = source.substring(start, current);
        advance();

        return new Token(TokenType.STRING_CONSTANT, lexeme, line);
    }

    private Token readIdentifier() {
        int start = current;

        while (isIdentifierPart(peek())) {
            advance();
        }

        String lexeme = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(lexeme, TokenType.IDENTIFIER);

        return new Token(type, lexeme, line);
    }

    private Token readSymbol() {
        char c = advance();
        return new Token(SYMBOLS.get(c), String.valueOf(c), line);
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) {
            advance();
        }
    }

    private void skipLineComment() {
        while (!isAtEnd() && peek() != '\n') {
            advance();
        }
    }

    private void skipBlockComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peek(1) == '/') {
                advance();
                advance();
                return;
            }
            advance();
        }

        throw new RuntimeException("Comentário de bloco não fechado na linha " + line);
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char peek() {
        return peek(0);
    }

    private char peek(int offset) {
        int pos = current + offset;
        if (pos >= source.length()) {
            return '\0';
        }
        return source.charAt(pos);
    }

    private char advance() {
        char c = source.charAt(current++);
        if (c == '\n') {
            line++;
        }
        return c;
    }
}
