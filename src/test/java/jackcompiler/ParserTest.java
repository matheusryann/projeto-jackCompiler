package jackcompiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
