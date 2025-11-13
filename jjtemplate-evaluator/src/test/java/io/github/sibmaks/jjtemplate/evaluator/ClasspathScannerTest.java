package io.github.sibmaks.jjtemplate.evaluator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sibmaks
 */
class ClasspathScannerTest {
    private ClassLoader classLoader;

    @BeforeEach
    void setUp() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    @AfterEach
    void tearDown() {
        Thread.currentThread().setContextClassLoader(this.classLoader);
    }

    @Test
    void testScanJar_jarProtocol_whenClassNotExists() throws Exception {
        var jar = File.createTempFile("test", ".jar");
        jar.deleteOnExit();

        try (var jos = new JarOutputStream(new FileOutputStream(jar))) {
            jos.putNextEntry(new JarEntry("com/test/pkg/A.class"));
            jos.write(new byte[]{1});
            jos.closeEntry();
        }

        var jarUrl = new URL("jar:" + jar.toURI() + "!/com/test/pkg/");

        var fake = new FakeClassLoader(jarUrl);
        var thread = Thread.currentThread();
        thread.setContextClassLoader(fake);

        var result = ClasspathScanner.findClasses("com.test.pkg");

        assertNotNull(result);
    }

    @Test
    void testScanJar_jarProtocol() throws Exception {
        var jar = File.createTempFile("test", ".jar");
        jar.deleteOnExit();
        var type = getClass();
        var packageName = type.getPackage().getName();

        var path = packageName.replace('.', '/') + '/';
        try (var jos = new JarOutputStream(new FileOutputStream(jar))) {
            jos.putNextEntry(new JarEntry(path + type.getSimpleName() + ".class"));
            jos.write(new byte[]{1});
            jos.closeEntry();
            jos.putNextEntry(new JarEntry(path + type.getSimpleName() + ".txt"));
            jos.write(new byte[]{1});
            jos.closeEntry();
            jos.putNextEntry(new JarEntry(type.getSimpleName() + ".md"));
            jos.write(new byte[]{1});
            jos.closeEntry();
        }

        var jarUrl = new URL("jar:" + jar.toURI() + "!/" + packageName.replace('.', '/') + "/");

        var fake = new FakeClassLoader(jarUrl);
        var thread = Thread.currentThread();
        thread.setContextClassLoader(fake);

        var result = ClasspathScanner.findClasses(packageName);

        assertNotNull(result);
    }

    @Test
    void testUnknownProtocol() throws Exception {
        var url = new URL("http://example.com");

        var fake = new FakeClassLoader(url);
        var thread = Thread.currentThread();
        thread.setContextClassLoader(fake);

        var result = ClasspathScanner.findClasses("com.test.pkg");
        assertTrue(result.isEmpty());
    }

    @Test
    void testResourcesIOException() {
        var fake = new ClassLoader() {
            @Override
            public Enumeration<URL> getResources(String name) {
                throw new RuntimeException(new IOException("boom"));
            }
        };

        var thread = Thread.currentThread();
        thread.setContextClassLoader(fake);

        var exception = assertThrows(RuntimeException.class, () -> ClasspathScanner.findClasses("x"));

        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void testScanDirectory_fileProtocol_whenNotExists() throws Exception {
        var root = File.createTempFile("scan", "");
        root.delete();
        root.mkdirs();

        var pkg = new File(root, "com/test/pkg");
        assertTrue(pkg.mkdirs());

        var classFile = new File(pkg, "MyClass.class");
        assertTrue(classFile.createNewFile());

        var txtFile = new File(pkg, "readme.txt");
        assertTrue(txtFile.createNewFile());

        var fileUrl = pkg.toURI().toURL();

        var fake = new FakeClassLoader(fileUrl);
        var thread = Thread.currentThread();
        thread.setContextClassLoader(fake);

        var result = ClasspathScanner.findClasses("com.test.pkg");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testScanDirectory_fileProtocol_whenDirectoryNotExists() throws Exception {
        var root = File.createTempFile("scan", "");
        root.delete();
        root.mkdirs();

        var pkg = new File(root, "com/test/pkg");

        var fileUrl = pkg.toURI().toURL();

        var fake = new FakeClassLoader(fileUrl);
        var thread = Thread.currentThread();
        thread.setContextClassLoader(fake);

        var result = ClasspathScanner.findClasses("com.test.pkg");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testScanDirectory_fileProtocol() throws Exception {
        var root = File.createTempFile("scan", "");
        root.delete();
        root.mkdirs();

        var type = getClass();
        var packageName = type.getPackage().getName();
        var pkg = new File(root, packageName.replace(".", "/"));
        assertTrue(pkg.mkdirs());

        var classFile = new File(pkg, type.getSimpleName() + ".class");
        assertTrue(classFile.createNewFile());

        var txtFile = new File(pkg, "readme.txt");
        assertTrue(txtFile.createNewFile());

        var fileUrl = pkg.toURI().toURL();

        var fake = new FakeClassLoader(fileUrl);
        var thread = Thread.currentThread();
        thread.setContextClassLoader(fake);

        var result = ClasspathScanner.findClasses(packageName);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testScanDirectory_withNestedFolder() throws Exception {
        var root = File.createTempFile("scanNest", "");
        root.delete();
        root.mkdirs();

        var pkg = new File(root, "com/test/pkg");
        assertTrue(pkg.mkdirs());

        var sub = new File(pkg, "sub");
        assertTrue(sub.mkdirs());

        var classFileParent = new File(pkg, "Parent.class");
        assertTrue(classFileParent.createNewFile());

        var classFileSub = new File(sub, "Child.class");
        assertTrue(classFileSub.createNewFile());

        new File(sub, "note.txt").createNewFile();
        new File(pkg, "temp.tmp").createNewFile();

        var fileUrl = pkg.toURI().toURL();

        var fake = new FakeClassLoader(fileUrl);
        var thread = Thread.currentThread();
        thread.setContextClassLoader(fake);

        var result = ClasspathScanner.findClasses("com.test.pkg");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    static class FakeClassLoader extends ClassLoader {

        private final List<URL> urls;

        FakeClassLoader(URL url) {
            this.urls = List.of(url);
        }

        @Override
        public Enumeration<URL> getResources(String name) {
            return Collections.enumeration(urls);
        }
    }

}