import lexer.Scanner;
import lexer.Token;
import parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ParserMain {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java -cp target/classes ParserMain <arquivo.jack>");
            System.err.println("   ou: mvn -q exec:java -Dexec.mainClass=ParserMain -Dexec.args=<arquivo.jack>");
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

            Parser parser = new Parser(tokens);
            String xml = parser.parse();

            String fileName = inputPath.getFileName().toString();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

            Path outputPath = inputPath.resolveSibling(baseName + "P.generated.xml");
            Files.writeString(outputPath, xml);

            System.out.println("XML sintático gerado com sucesso em:");
            System.out.println(outputPath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Erro de leitura/escrita: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println("Erro sintático: " + e.getMessage());
            System.exit(1);
        }
    }
}