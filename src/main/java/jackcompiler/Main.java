package jackcompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java -cp out jackcompiler.Main <arquivo.jack>");
            System.exit(1);
        }

        Path inputPath = Path.of(args[0]);

        if (!Files.exists(inputPath)) {
            System.err.println("Arquivo não encontrado: " + inputPath);
            System.exit(1);
        }

        if (!inputPath.toString().endsWith(".jack")) {
            System.err.println("O arquivo precisa ter extensão .jack");
            System.exit(1);
        }

        try {
            String source = Files.readString(inputPath);

            Scanner scanner = new Scanner(source);
            List<Token> tokens = scanner.tokenize();

            List<String> outputLines = new ArrayList<>();
            outputLines.add("<tokens>");

            for (Token token : tokens) {
                if (token.getType() != TokenType.EOF) {
                    outputLines.add(token.toXml());
                }
            }

            outputLines.add("</tokens>");

            String fileName = inputPath.getFileName().toString();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

            Path outputPath = inputPath.resolveSibling(baseName + "T.generated.xml");
            Files.write(outputPath, outputLines);

            System.out.println("XML gerado com sucesso em:");
            System.out.println(outputPath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Erro de leitura/escrita: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println("Erro léxico: " + e.getMessage());
            System.exit(1);
        }
    }
}