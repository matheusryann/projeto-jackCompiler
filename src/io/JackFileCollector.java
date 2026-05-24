package io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Resolve a entrada do compilador: um arquivo {@code .jack} ou todos os {@code .jack}
 * de um diretório (nível atual, sem subpastas).
 */
public final class JackFileCollector {

    private JackFileCollector() {
    }

    /**
     * @param input caminho para {@code Foo.jack} ou para uma pasta com arquivos Jack
     * @return lista de arquivos a compilar (ordenada por nome)
     */
    public static List<Path> collect(Path input) throws IOException {
        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Caminho não encontrado: " + input);
        }

        if (Files.isRegularFile(input)) {
            requireJackExtension(input);
            return List.of(input);
        }

        if (Files.isDirectory(input)) {
            try (Stream<Path> stream = Files.list(input)) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(JackFileCollector::isJackFile)
                        .sorted()
                        .toList();
            }
        }

        throw new IllegalArgumentException(
                "Entrada inválida (nem arquivo .jack nem diretório): " + input);
    }

    private static boolean isJackFile(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".jack");
    }

    private static void requireJackExtension(Path file) {
        if (!isJackFile(file)) {
            throw new IllegalArgumentException("Arquivo precisa ter extensão .jack: " + file);
        }
    }
}
