package org.example.databases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.products.Product;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.List;

public class H2InMemoryDb implements AutoCloseable {

    private SessionFactory sessionFactory;
    protected final Logger logger = LogManager.getLogger(H2InMemoryDb.class);

    public H2InMemoryDb() {
        this.sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    public void save(List<Product> products) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            for (Product product : products) {
                session.save(product);
            }
            session.getTransaction().commit();
        }
    }

    public List<Product> search() {

        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("FROM Product ", Product.class);
            List<Product> products = query.getResultList();

            for (Product product : products) {
                logger.info("SKU: {}", product.getSKU());
                logger.info("Product Title: {}", product.getProductTitle());
                logger.info("Category: {}", product.getCategory());
                logger.info("Price: {}", product.getPrice());
                logger.info("---------------------");
//            }

            }
            return products;
        }

    }

    @Override
    public void close() throws Exception {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}