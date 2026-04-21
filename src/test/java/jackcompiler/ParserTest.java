package jackcompiler;

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

        // Filtrar tokens diferentes de EOF. Tirar o EOF evita que fique um token extra na lista 
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
}
