package org.example.products;

import com.google.gson.JsonObject;

import javax.persistence.Entity;

@Entity
public class DebenhamsProduct extends Product {

    public static DebenhamsProduct productFromJson(JsonObject jsonObject) {
        DebenhamsProduct asosProduct = new DebenhamsProduct();
        asosProduct.setSKU(jsonObject.get("productKey").getAsString());
        asosProduct.setProductTitle(jsonObject.get("name").getAsString());
//              For the purpose of this test get only current price
        asosProduct.setPrice(jsonObject.get("price").getAsString());
        return asosProduct;
    }

}
