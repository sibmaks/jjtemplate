package io.github.sibmaks.jjtemplate.compiler.runtime;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClasspathScannerTest {

    @Test
    void shouldResolveJarFileFromEncodedJarUrl() throws Exception {
        var dir = Files.createTempDirectory("jjtemplate path with spaces");
        var jarPath = Files.createFile(dir.resolve("sample.jar"));
        var jarResource = new URL("jar:" + jarPath.toUri().toURL() + "!/io/github/sibmaks");

        var resolved = ClasspathScanner.resolveJarFile(jarResource);

        assertEquals(jarPath.toFile().getAbsolutePath(), resolved.getAbsolutePath());
    }
}
