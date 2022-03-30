package com.svcline.models;

import java.net.HttpURLConnection;

public class LineResponse {
    private int code;
    private String message;
    private String json;

    public LineResponse() {

    }

    public LineResponse(int code, String json) {
        this.code = code;
        this.json = json;
    }

    public LineResponse(String json) {
        this.code = HttpURLConnection.HTTP_OK;
        this.json = json;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
