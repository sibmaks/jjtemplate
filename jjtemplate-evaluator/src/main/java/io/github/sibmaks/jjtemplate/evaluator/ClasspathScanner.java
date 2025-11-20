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
import java.util.jar.JarEntry;
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

    private static final String CLASS_SUFFIX = ".class";
    private static final int MAX_JAR_ENTRIES = 100_000;
    private static final long MAX_JAR_SIZE_BYTES = 1024L * 1024L * 1024L; // 1Gb safety limit

    /**
     * Extracted nested try block
     */
    private static void processJarEntry(
            JarEntry entry,
            String path,
            List<Class<?>> classes
    ) {
        var name = entry.getName();

        if (!name.startsWith(path) || !name.endsWith(CLASS_SUFFIX)) {
            return;
        }
        var className = name
                .replace('/', '.')
                .substring(0, name.length() - CLASS_SUFFIX.length());

        tryLoadClass(className, classes);
    }

    private static void tryLoadClass(
            String className,
            List<Class<?>> classes
    ) {
        try {
            classes.add(Class.forName(className));
        } catch (Throwable ignored) {
            // intentionally ignored
        }
    }

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
            throw new IllegalArgumentException("Invalid URL passed", e);
        }
        return classes;
    }

    private static void scanJar(URL resource, String path, List<Class<?>> classes) {
        var rawPath = resource.getPath();
        var jarPath = rawPath.substring(5, rawPath.indexOf("!"));

        var file = new File(jarPath);

        if (!file.exists()) {
            throw new IllegalStateException("JAR file does not exist: " + jarPath);
        }

        if (file.length() > MAX_JAR_SIZE_BYTES) {
            throw new IllegalStateException("JAR file too large, possible zip bomb: " + jarPath);
        }

        try (var jarFile = new JarFile(file, true)) {
            var entries = jarFile.entries();
            var entryCount = 0;

            while (entries.hasMoreElements()) {
                entryCount++;

                if (entryCount > MAX_JAR_ENTRIES) {
                    throw new IllegalStateException("Too many JAR entries, possible zip bomb: " + jarPath);
                }

                var entry = entries.nextElement();
                processJarEntry(entry, path, classes);
            }

        } catch (IOException e) {
            throw new IllegalStateException("Error scanning JAR file: " + jarPath, e);
        }
    }

    private static void scanDirectory(
            File directory,
            String basePackage,
            List<Class<?>> classes
    ) {
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
            } else if (file.getName().endsWith(CLASS_SUFFIX)) {
                var className = basePackage + '.' +
                        file.getName().substring(0, file.getName().length() - CLASS_SUFFIX.length());

                tryLoadClass(className, classes);
            }
        }
    }

}
