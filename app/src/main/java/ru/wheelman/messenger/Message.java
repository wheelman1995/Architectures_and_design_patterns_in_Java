package ru.wheelman.messenger;

public class Message {
    private String text;
    private String type;
    private long time;

    public Message(String text, String type, long time) {
        this.text = text;
        this.type = type;
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public long getTime() {
        return time;
    }
}
