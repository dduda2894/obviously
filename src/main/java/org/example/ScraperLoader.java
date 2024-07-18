package org.example;

import com.sun.tools.javac.Main;
import org.example.scrapers.Scraper;
import org.example.utils.FileUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;


public class ScraperLoader {

    public static Scraper getScraperInstance(Path scrapersDirectoryPath, String scraperName) {
        // List to hold all Java files
        List<Path> javaFiles = FileUtil.findAllFilesWithExtension(scrapersDirectoryPath, ".java");
        Scraper scraper = null;
        for (Path javaFile : javaFiles) {
            String className = getClassName(javaFile, scrapersDirectoryPath);
            try {
                Class<?> clazz = Class.forName(className);
                if (containsStaticPropertyWithValue(clazz, "scraperName", scraperName)) {
                    System.out.println("Class containing the static property found: " + className);
                    scraper = (Scraper) clazz.getDeclaredConstructor().newInstance();
                    break;
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return scraper;
    }

    private static String getClassName(Path javaFile, Path startPath) {
        // Convert file path to class name
        Path relativePath = startPath.relativize(javaFile);
        Path sourcesRootPath = Path.of("org.example.scrapers");
        Path classPath = sourcesRootPath.resolve(relativePath);
        String className = classPath.toString().replace(".java", "").replace(File.separator, ".");
        return className;
    }

    private static boolean containsStaticPropertyWithValue(Class<?> clazz, String propertyName, String value) {
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(propertyName) && java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class)) {
                    field.setAccessible(true);
                    if (value.equals(field.get(null))) {
                        return true;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

}
