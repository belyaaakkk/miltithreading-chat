package com.belyak;

public class Message {
    private final String sender;
    private final String message;

    public Message(final String sender, final String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return this.sender;
    }

    @Override
    public String toString() {
        return this.sender + ": " + this.message;
    }
}
