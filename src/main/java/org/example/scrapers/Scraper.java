package org.example.scrapers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.products.Product;
import java.util.List;

public abstract class Scraper {


    public String url;
    protected final Logger logger;

    public Scraper(Class clazz) {
        this.logger = LogManager.getLogger(clazz);
    }

    public abstract List<Product> scrape();


}
