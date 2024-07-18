package org.example.products;

import com.google.gson.JsonObject;

import javax.persistence.Entity;

@Entity
public class AsosProduct extends Product {

    public static AsosProduct productFromJson(JsonObject jsonObject) {
        AsosProduct asosProduct = new AsosProduct();
        asosProduct.setSKU(jsonObject.get("id").getAsString());
        asosProduct.setProductTitle(jsonObject.get("name").getAsString());
//              For the purpose of this test get only current price
        asosProduct.setPrice(jsonObject.get("price").getAsJsonObject().get("current").getAsJsonObject().get("text").getAsString());
        return asosProduct;
    }

}
