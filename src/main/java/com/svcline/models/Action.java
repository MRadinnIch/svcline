package com.svcline.models;

public class Action {
    private static final String COLOR_BG = "0xFF0000FF";
    private static final String COLOR_TXT = "0xFF000000";

    private String text;
    private String bgColor;
    private String textColor;
    private State state;

    public Action() {
    }

    public Action(String text, State state) {
        this.text = text;
        this.state = state;
        this.bgColor = COLOR_BG;
        this.textColor = COLOR_TXT;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
