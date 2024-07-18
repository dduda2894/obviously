package org.example.utils;

import com.sun.tools.javac.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResourcesUtil {
    public static Properties loadConfig(String resourceName) {
        Properties properties = new Properties();
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                System.err.println("Resource not found: " + resourceName);
                return null;
            }
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return properties;
    }
}
