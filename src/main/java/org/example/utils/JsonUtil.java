package org.example.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonUtil {

    public static JsonElement parse(String jsonString) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(jsonString);
        return jsonElement;
    }
}
