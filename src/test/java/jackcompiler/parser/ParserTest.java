package jackcompiler.parser;

import jackcompiler.lexer.Scanner;
import jackcompiler.lexer.Token;
import jackcompiler.lexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTest {

    @Test
    void parseEmptyClass_emitsClassXml() {
        String jack = """
                class Main {
                }
                """;

        Scanner scanner = new Scanner(jack);
        Parser parser = new Parser(scanner.tokenize());

        String expected = """
                <class>
                  <keyword> class </keyword>
                  <identifier> Main </identifier>
                  <symbol> { </symbol>
                  <symbol> } </symbol>
                </class>""";

        assertEquals(expected, parser.parse());
    }

    @Test
    void parseTerm_integerConstant() {
        String jack = "10";
        Scanner scanner = new Scanner(jack);
        List<Token> tokens = scanner.tokenize().stream()
                .filter(t -> t.getType() != TokenType.EOF)
                .toList();

        Parser parser = new Parser(tokens);
        String xml = parser.parseTerm();

        assertTrue(xml.contains("<term>"));
        assertTrue(xml.contains("<integerConstant> 10 </integerConstant>"));
        String expected = """
                <term>
                  <integerConstant> 10 </integerConstant>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_stringConstant() {
        String jack = "\"hello\"";
        Scanner scanner = new Scanner(jack);
        List<Token> tokens = scanner.tokenize().stream()
                .filter(t -> t.getType() != TokenType.EOF)
                .toList();

        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <stringConstant> hello </stringConstant>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_keywordConstant_true() {
        String jack = "true";
        Scanner scanner = new Scanner(jack);
        List<Token> tokens = scanner.tokenize().stream()
                .filter(t -> t.getType() != TokenType.EOF)
                .toList();

        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <keyword> true </keyword>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_identifierOnly() {
        String jack = "foo";
        Scanner scanner = new Scanner(jack);

        List<Token> tokens = scanner.tokenize().stream()
                .filter(t -> t.getType() != TokenType.EOF)
                .toList();

        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <identifier> foo </identifier>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_parenthesizedExpression() {
        List<Token> tokens = tokensWithoutEof("(x + y)");
        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <symbol> ( </symbol>
                  <expression>
                    <term>
                      <identifier> x </identifier>
                    </term>
                    <symbol> + </symbol>
                    <term>
                      <identifier> y </identifier>
                    </term>
                  </expression>
                  <symbol> ) </symbol>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_unaryMinus() {
        List<Token> tokens = tokensWithoutEof("-j");
        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <symbol> - </symbol>
                  <term>
                    <identifier> j </identifier>
                  </term>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_arrayIndex() {
        List<Token> tokens = tokensWithoutEof("a[2]");
        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <identifier> a </identifier>
                  <symbol> [ </symbol>
                  <expression>
                    <term>
                      <integerConstant> 2 </integerConstant>
                    </term>
                  </expression>
                  <symbol> ] </symbol>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_subroutineCall() {
        List<Token> tokens = tokensWithoutEof("SquareGame.new()");
        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <identifier> SquareGame </identifier>
                  <symbol> . </symbol>
                  <identifier> new </identifier>
                  <symbol> ( </symbol>
                  <expressionList>
                  </expressionList>
                  <symbol> ) </symbol>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseExpression_twoTermsAndOp() {
        List<Token> tokens = tokensWithoutEof("1 + 2");
        String xml = new Parser(tokens).parseExpression();
        String expected = """
                <expression>
                  <term>
                    <integerConstant> 1 </integerConstant>
                  </term>
                  <symbol> + </symbol>
                  <term>
                    <integerConstant> 2 </integerConstant>
                  </term>
                </expression>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_unaryTilde() {
        List<Token> tokens = tokensWithoutEof("~flag");
        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <symbol> ~ </symbol>
                  <term>
                    <identifier> flag </identifier>
                  </term>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_simpleSubroutineCall_withoutDot() {
        List<Token> tokens = tokensWithoutEof("draw()");
        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <identifier> draw </identifier>
                  <symbol> ( </symbol>
                  <expressionList>
                  </expressionList>
                  <symbol> ) </symbol>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_subroutineCall_withTwoArguments() {
        List<Token> tokens = tokensWithoutEof("Math.min(x, y)");
        String xml = new Parser(tokens).parseTerm();
        String expected = """
                <term>
                  <identifier> Math </identifier>
                  <symbol> . </symbol>
                  <identifier> min </identifier>
                  <symbol> ( </symbol>
                  <expressionList>
                    <expression>
                      <term>
                        <identifier> x </identifier>
                      </term>
                    </expression>
                    <symbol> , </symbol>
                    <expression>
                      <term>
                        <identifier> y </identifier>
                      </term>
                    </expression>
                  </expressionList>
                  <symbol> ) </symbol>
                </term>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseExpression_chainedOperators() {
        List<Token> tokens = tokensWithoutEof("1 + 2 + 3");
        String xml = new Parser(tokens).parseExpression();
        String expected = """
                <expression>
                  <term>
                    <integerConstant> 1 </integerConstant>
                  </term>
                  <symbol> + </symbol>
                  <term>
                    <integerConstant> 2 </integerConstant>
                  </term>
                  <symbol> + </symbol>
                  <term>
                    <integerConstant> 3 </integerConstant>
                  </term>
                </expression>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseExpression_comparisonAndLogical() {
        List<Token> tokens = tokensWithoutEof("a < b & c | d");
        String xml = new Parser(tokens).parseExpression();
        String expected = """
                <expression>
                  <term>
                    <identifier> a </identifier>
                  </term>
                  <symbol> &lt; </symbol>
                  <term>
                    <identifier> b </identifier>
                  </term>
                  <symbol> &amp; </symbol>
                  <term>
                    <identifier> c </identifier>
                  </term>
                  <symbol> | </symbol>
                  <term>
                    <identifier> d </identifier>
                  </term>
                </expression>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseExpression_parenthesesWithUnaryInside() {
        List<Token> tokens = tokensWithoutEof("i * (-j)");
        String xml = new Parser(tokens).parseExpression();
        String expected = """
                <expression>
                  <term>
                    <identifier> i </identifier>
                  </term>
                  <symbol> * </symbol>
                  <term>
                    <symbol> ( </symbol>
                    <expression>
                      <term>
                        <symbol> - </symbol>
                        <term>
                          <identifier> j </identifier>
                        </term>
                      </term>
                    </expression>
                    <symbol> ) </symbol>
                  </term>
                </expression>""";
        assertEquals(expected, xml);
    }

    @Test
    void parseTerm_keywordConstants_false_null_this() {
        assertEquals("""
                <term>
                  <keyword> false </keyword>
                </term>""", new Parser(tokensWithoutEof("false")).parseTerm());

        assertEquals("""
                <term>
                  <keyword> null </keyword>
                </term>""", new Parser(tokensWithoutEof("null")).parseTerm());

        assertEquals("""
                <term>
                  <keyword> this </keyword>
                </term>""", new Parser(tokensWithoutEof("this")).parseTerm());
    }

    private static List<Token> tokensWithoutEof(String jack) {
        Scanner scanner = new Scanner(jack);
        return scanner.tokenize().stream()
                .filter(t -> t.getType() != TokenType.EOF)
                .toList();
    }
}
