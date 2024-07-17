package org.example.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;

public class JsoupUtil {


    public static Connection.Response execute(String url, Map<String, String> headers) throws IOException {
        Connection.Response execute = Jsoup.connect(url).ignoreContentType(true).headers(headers).execute();
        return execute;
    }
}
