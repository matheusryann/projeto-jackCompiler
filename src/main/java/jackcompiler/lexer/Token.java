package jackcompiler.lexer;

public class Token {
    private TokenType type;
    private String lexeme;
    private int line;

    public Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    /** Saída XML no formato Nand2Tetris: {@code <tag> valor </tag>}. */
    public String toXml() {
        String tag = xmlTag();
        return "<" + tag + "> " + escapeXml() + " </" + tag + ">";
    }

    private String xmlTag() {
        if (type == TokenType.INTEGER_CONSTANT) {
            return "integerConstant";
        }
        if (type == TokenType.STRING_CONSTANT) {
            return "stringConstant";
        }
        if (type == TokenType.IDENTIFIER) {
            return "identifier";
        }
        if (type == TokenType.EOF) {
            throw new IllegalStateException("toXml() não se aplica a EOF");
        }
        if (type.name().startsWith("KEYWORD_")) {
            return "keyword";
        }
        return "symbol";
    }

    private String escapeXml() {
        String text = lexeme;
        if (type == TokenType.STRING_CONSTANT) {
            text = text.replace("\"", "");
        }
        text = text.replace("&", "&amp;");
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        text = text.replace("\"", "&quot;");
        return text;
    }
}
