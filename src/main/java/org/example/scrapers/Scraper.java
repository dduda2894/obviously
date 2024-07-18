package org.example.scrapers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Scraper {

    protected final Logger logger;

    public Scraper(Class clazz) {
        this.logger = LogManager.getLogger(clazz);
    }

    public abstract Object scrape();



}
