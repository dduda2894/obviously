package org.example.products;

import javax.persistence.*;
import java.util.Objects;

@Entity

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "productType", discriminatorType = DiscriminatorType.STRING)
@Table(name = "products") // Use the same table for both entities
public class Product {
    private String SKU;
    private String price;
    private String category;
    private String productTitle;

    public Product(String SKU, String price, String category, String productTitle) {
        this.SKU = SKU;
        this.price = price;
        this.category = category;
        this.productTitle = productTitle;
    }

    public Product() {
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(SKU, product.SKU) && Objects.equals(price, product.price) && Objects.equals(category, product.category) && Objects.equals(productTitle, product.productTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SKU, price, category, productTitle);
    }

    @Override
    public String toString() {
        return "Product{" + "SKU='" + SKU + '\'' + ", price='" + price + '\'' + ", category='" + category + '\'' + ", productTitle='" + productTitle + '\'' + '}';
    }

}
