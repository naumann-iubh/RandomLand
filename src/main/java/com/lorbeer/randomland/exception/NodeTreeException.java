package com.lorbeer.randomland.exception;

public class NodeTreeException extends Exception {
    private final String id;

    public NodeTreeException(String message, String id) {
        super(message);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
