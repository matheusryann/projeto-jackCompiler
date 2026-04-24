package jackcompiler.lexer;

/**
 * Tipos de tokens da linguagem Jack (Nand2Tetris).
 * Na saída XML, keywords e símbolos mapeiam para as tags {@code keyword} e {@code symbol}.
 */
public enum TokenType {

    // Literais e identificador
    INTEGER_CONSTANT,
    STRING_CONSTANT,
    IDENTIFIER,

    // Símbolos (um caractere cada)
    LBRACE,    // {
    RBRACE,    // }
    LPAREN,    // (
    RPAREN,    // )
    LBRACKET,  // [
    RBRACKET,  // ]
    DOT,       // .
    COMMA,     // ,
    SEMICOLON, // ;
    PLUS,      // +
    MINUS,     // -
    STAR,      // *
    SLASH,     // /
    AND,       // &
    PIPE,      // |
    LT,        // <
    GT,        // >
    EQ,        // =
    TILDE,     // ~

    // Keywords (ordem alfabética auxiliar)
    KEYWORD_BOOLEAN,
    KEYWORD_CHAR,
    KEYWORD_CLASS,
    KEYWORD_CONSTRUCTOR,
    KEYWORD_DO,
    KEYWORD_ELSE,
    KEYWORD_FALSE,
    KEYWORD_FIELD,
    KEYWORD_FUNCTION,
    KEYWORD_IF,
    KEYWORD_INT,
    KEYWORD_LET,
    KEYWORD_METHOD,
    KEYWORD_NULL,
    KEYWORD_RETURN,
    KEYWORD_STATIC,
    KEYWORD_THIS,
    KEYWORD_TRUE,
    KEYWORD_VAR,
    KEYWORD_VOID,
    KEYWORD_WHILE,

    /** Fim do arquivo fonte */
    EOF
}
