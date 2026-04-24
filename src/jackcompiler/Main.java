package jackcompiler;

import jackcompiler.io.TokensXmlWriter;
import jackcompiler.lexer.Scanner;
import jackcompiler.lexer.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Ponto de entrada: executa o analisador léxico e grava {@code *T.generated.xml} (tokenizer).
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java -cp target/classes jackcompiler.Main <arquivo.jack>");
            System.err.println("   ou: mvn -q compile exec:java \"-Dexec.args=<arquivo.jack>\"");
            System.err.println("(a partir da pasta do projeto; rode mvn compile antes)");
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

            Path outputPath = TokensXmlWriter.writeBesideJackFile(inputPath, tokens);

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
