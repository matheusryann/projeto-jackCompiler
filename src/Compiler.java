import io.JackFileCollector;
import vm.VMWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Ponto de entrada do compilador Jack (Project 11).
 * Aceita um arquivo .jack ou um diretorio com varios .jack.
 */
public class Compiler {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java -cp target/classes Compiler <arquivo.jack | diretorio>");
            System.exit(1);
        }

        Path input = Path.of(args[0]);

        try {
            List<Path> jackFiles = JackFileCollector.collect(input);

            if (jackFiles.isEmpty()) {
                System.err.println("Nenhum arquivo .jack encontrado em: " + input);
                System.exit(1);
            }

            for (Path jackFile : jackFiles) {
                compileJackFile(jackFile);
                System.out.println("Gerado: " + vmPathFor(jackFile).toAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Esqueleto: cria o .vm ao lado do .jack.
     * Fase 2: Scanner + CompilationEngine escrevem no VMWriter.
     */
    private static void compileJackFile(Path jackFile) throws IOException {
        Path vmFile = vmPathFor(jackFile);

        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(vmFile));
             VMWriter vm = new VMWriter(pw)) {
            // vazio por enquanto — Fase 2
        }
    }

    /** Foo.jack -> Foo.vm no mesmo diretorio. */
    private static Path vmPathFor(Path jackFile) {
        String name = jackFile.getFileName().toString();
        String base = name.substring(0, name.lastIndexOf('.'));
        return jackFile.resolveSibling(base + ".vm");
    }
}
