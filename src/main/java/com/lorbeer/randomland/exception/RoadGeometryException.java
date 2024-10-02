package com.lorbeer.randomland.exception;

public class RoadGeometryException extends Exception {
    private final String id;

    public RoadGeometryException(String message, String id) {
        super(message);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
