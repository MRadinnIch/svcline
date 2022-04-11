package com.svcline.models;

import com.google.gson.Gson;

import java.net.HttpURLConnection;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

public class LineResponse {
    private int code;
    private String message;
    private String json;

    public LineResponse() {

    }

    public LineResponse(int code, Object obj) {
        Gson gson = new Gson();

        this.code = code;
        this.json = gson.toJson(obj);
    }

    public LineResponse(Object obj) {
        Gson gson = new Gson();

        this.code = HTTP_OK;
        this.json = gson.toJson(obj);
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

    public boolean succeeded() {
        return code == HTTP_OK || code == HTTP_CREATED;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return "LineResponse{" +
               "code=" + code +
               ", json='" + json + '\'' +
               '}';
    }
}
