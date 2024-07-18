package org.example.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;

public class JsoupUtil {


    public static Connection.Response execute(String url, Map<String, String> headers, Connection.Method method) throws IOException {
        Connection.Response response = Jsoup.connect(url).ignoreHttpErrors(true).ignoreContentType(true).headers(headers).method(method).execute();
        return response;
    }

    public static Connection.Response execute(String url, Map<String, String> headers, Connection.Method method,String body) throws IOException {
        Connection.Response response = Jsoup.connect(url).ignoreHttpErrors(true).ignoreContentType(true).headers(headers).method(method).requestBody(body).execute();
        return response;
    }
}
