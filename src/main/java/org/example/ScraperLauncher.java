package org.example;

import org.example.databases.H2InMemoryDb;
import org.example.products.Product;
import org.example.scrapers.Scraper;
import org.example.utils.ResourcesUtil;

import java.nio.file.*;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ScraperLauncher {


    public static void main(String[] args) throws Exception {
        System.out.println("User saved successfully!");
        String configFileName = "config.properties";
//        Read properties file
        Properties prop = ResourcesUtil.loadConfig(configFileName);
//        Get scraper name
        String scraperName = prop.getProperty("scraper");
        Path startPath = Paths.get("src/main/java/org/example/scrapers"); // specify the start directory
//        Create scraper class instance
        Scraper scraper = ScraperLoader.getScraperInstance(startPath, scraperName);
//        Launch scraper
        List<Product> products = (List) scraper.scrape();
//   Save products into in-memory database.
//   Then search for the products in print them in the console.
        try (H2InMemoryDb h2InMemoryDb = new H2InMemoryDb()) {
            h2InMemoryDb.save(products.stream().limit(10).collect(Collectors.toList()));
            h2InMemoryDb.search();
        }
    }


}

