package org.example;

import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;

import java.util.Map;

public class InterceptedHttpRequest {

    private HttpRequest request;

    private HttpMethod method;
    private String url;
    private Map<String, String> headers;

    @Override
    public String toString() {
        return "InterceptedHttpRequest{" +
                "method=" + method +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                '}';
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }
}
