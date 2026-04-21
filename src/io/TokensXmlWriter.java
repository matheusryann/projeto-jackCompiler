package jackcompiler.io;

import jackcompiler.lexer.Token;
import jackcompiler.lexer.TokenType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Escreve a saída do tokenizer no formato Nand2Tetris ({@code <tokens>} … {@code </tokens>}).
 */
public final class TokensXmlWriter {

    private TokensXmlWriter() {
    }

    /**
     * Gera {@code BaseNameT.generated.xml} ao lado do ficheiro {@code BaseName.jack}.
     *
     * @return caminho absoluto do ficheiro escrito
     */
    public static Path writeBesideJackFile(Path jackFile, List<Token> tokens) throws IOException {
        List<String> outputLines = new ArrayList<>();
        outputLines.add("<tokens>");
        for (Token token : tokens) {
            if (token.getType() != TokenType.EOF) {
                outputLines.add(token.toXml());
            }
        }
        outputLines.add("</tokens>");

        String fileName = jackFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        Path outputPath = jackFile.resolveSibling(baseName + "T.generated.xml");
        Files.write(outputPath, outputLines);
        return outputPath;
    }
}
