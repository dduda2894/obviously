package org.example.products;

import com.google.gson.JsonObject;

import java.util.Objects;

public class AsosProduct extends Product {

    private String SKU;
    private String price;
    private String category;
    private String productTitle;

    public String getSKU() {
        return SKU;
    }

    public static AsosProduct productFromJson(JsonObject jsonObject) {
        AsosProduct asosProduct = new AsosProduct();
        asosProduct.setSKU(jsonObject.get("id").getAsString());
        asosProduct.setProductTitle(jsonObject.get("name").getAsString());
//              For the purpose of this test get only current price
        asosProduct.setPrice(jsonObject.get("price").getAsJsonObject().get("current").getAsJsonObject().get("text").getAsString());
        return asosProduct;
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
        AsosProduct product = (AsosProduct) o;
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
