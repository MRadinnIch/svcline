package com.svcline.models;

import com.svcline.svcline;

public class Action {
    private String text;
    private String bgColor;
    private String textColor;
    private State state;

    public Action() {
    }

    public Action(String text, State state) {
        this.text = text;
        this.state = state;
        this.bgColor = svcline.getButtonColorBg();
        this.textColor = svcline.getButtonColorTxt();
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
