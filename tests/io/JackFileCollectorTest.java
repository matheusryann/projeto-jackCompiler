package io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JackFileCollectorTest {

    @Test
    void collect_singleJackFile(@TempDir Path temp) throws IOException {
        Path jack = temp.resolve("Main.jack");
        Files.writeString(jack, "class Main { }");

        List<Path> result = JackFileCollector.collect(jack);

        assertEquals(1, result.size());
        assertEquals(jack, result.get(0));
    }

    @Test
    void collect_directory_returnsAllJackFilesSorted(@TempDir Path temp) throws IOException {
        Files.writeString(temp.resolve("B.jack"), "");
        Files.writeString(temp.resolve("A.jack"), "");
        Files.writeString(temp.resolve("C.jack"), "");
        Files.writeString(temp.resolve("readme.txt"), "ignore");

        List<Path> result = JackFileCollector.collect(temp);

        assertEquals(3, result.size());
        assertTrue(result.get(0).endsWith("A.jack"));
        assertTrue(result.get(1).endsWith("B.jack"));
        assertTrue(result.get(2).endsWith("C.jack"));
    }

    @Test
    void collect_emptyDirectory_returnsEmptyList(@TempDir Path temp) throws IOException {
        List<Path> result = JackFileCollector.collect(temp);
        assertEquals(0, result.size());
    }

    @Test
    void collect_missingPath_throws() {
        Path missing = Path.of("caminho-inexistente-" + System.nanoTime());
        assertThrows(IllegalArgumentException.class, () -> JackFileCollector.collect(missing));
    }

    @Test
    void collect_nonJackFile_throws(@TempDir Path temp) throws IOException {
        Path txt = temp.resolve("foo.txt");
        Files.writeString(txt, "x");
        assertThrows(IllegalArgumentException.class, () -> JackFileCollector.collect(txt));
    }
}
