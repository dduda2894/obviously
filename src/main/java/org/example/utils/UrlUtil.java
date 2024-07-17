package org.example.utils;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtil {

    public static String replaceQueryParameter(String previousUrl, String parameter, String value) {
        try {
            String currentUrl = new URIBuilder(previousUrl).setParameter(parameter, value)
                    .toString();
            return currentUrl;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
