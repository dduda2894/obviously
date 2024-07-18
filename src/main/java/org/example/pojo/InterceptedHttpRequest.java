package org.example.pojo;

import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;

import java.util.Arrays;
import java.util.Map;

public class InterceptedHttpRequest {

    private HttpRequest request;

    private HttpMethod method;
    private String url;
    private Map<String, String> headers;

    private String content;

    @Override
    public String toString() {
        return "InterceptedHttpRequest{" +
                "request=" + request +
                ", method=" + method +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", content='" + content + '\'' +
                '}';
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
