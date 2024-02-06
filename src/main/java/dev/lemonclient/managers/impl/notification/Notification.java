package dev.lemonclient.managers.impl.notification;

import dev.lemonclient.utils.Utils;

public class Notification {
    public Type type;
    public String title;
    public String text;
    public int showTime = 1500, maxShowTime = 1500;

    public double x, y;
    public boolean startUpdated, willRemove;

    public Notification(Type type, String title, String text) {
        this.withType(type).withTitle(title).withText(text);
        startUpdated = true;
        willRemove = false;
        this.x = Utils.getWindowWidth();
        this.y = Utils.getWindowHeight();
    }

    public Notification withType(Type type) {
        this.type = type;
        return this;
    }

    public Notification withTitle(String s) {
        this.title = s;
        return this;
    }

    public Notification withText(String s) {
        this.text = s;
        return this;
    }

    public Notification withShowTime(int t) {
        this.showTime = t;
        this.maxShowTime = t;
        return this;
    }

    public void update() {
        if (showTime > 0) {
            showTime--;
        }
    }

    public void destroy() {
        type = null;
        title = null;
        text = null;
        showTime = maxShowTime = 0;
    }

    public void remove() {
        showTime = maxShowTime = 0;
        willRemove = true;
    }

    public enum Type {
        INFO,
        SUCCESS,
        WARING,
        ERROR
    }
}
