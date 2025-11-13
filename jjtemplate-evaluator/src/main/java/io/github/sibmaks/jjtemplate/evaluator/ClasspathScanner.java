package io.github.sibmaks.jjtemplate.evaluator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;

/**
 * Utility for scanning the classpath and locating classes inside directories and JAR files.
 *
 * <p>Supports scanning both file-based class directories and packaged JAR archives.
 * Used to detect and load classes under a specific base package.</p>
 *
 * <p>Only classes ending in {@code .class} are considered. Errors during class loading
 * are ignored silently.</p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClasspathScanner {

    /**
     * Finds and loads all classes located under the specified base package.
     *
     * @param basePackage package name to scan, e.g. {@code com.example.myapp}
     * @return list of discovered classes (maybe empty)
     * @throws RuntimeException if classpath resources cannot be accessed
     */
    public static List<Class<?>> findClasses(String basePackage) {
        var classes = new ArrayList<Class<?>>();
        var path = basePackage.replace('.', '/');
        try {
            var resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(path);

            while (resources.hasMoreElements()) {
                var resource = resources.nextElement();
                switch (resource.getProtocol()) {
                    case "jar":
                        scanJar(resource, path, classes);
                        break;

                    case "file":
                        scanDirectory(new File(resource.toURI()), basePackage, classes);
                        break;

                    default:
                        break;
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    private static void scanJar(URL resource, String path, List<Class<?>> classes) {
        var jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
        try (var jarFile = new JarFile(jarPath)) {
            var entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                var name = entry.getName();

                if (name.startsWith(path) && name.endsWith(".class")) {
                    var className = name.replace('/', '.')
                            .replace(".class", "");

                    try {
                        classes.add(Class.forName(className));
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void scanDirectory(File directory, String basePackage, List<Class<?>> classes) {
        if (!directory.exists()) {
            return;
        }

        var files = Optional.ofNullable(directory.listFiles())
                .orElseGet(() -> new File[0]);

        for (var file : files) {
            if (file.isDirectory()) {
                scanDirectory(
                        file,
                        basePackage + "." + file.getName(),
                        classes
                );
            } else if (file.getName().endsWith(".class")) {
                var className = basePackage + '.' + file.getName().substring(0, file.getName().length() - 6);

                try {
                    classes.add(Class.forName(className));
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
