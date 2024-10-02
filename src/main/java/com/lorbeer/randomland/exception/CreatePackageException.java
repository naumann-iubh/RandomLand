package com.lorbeer.randomland.exception;

public class CreatePackageException extends Exception {
    private final String id;

    public CreatePackageException(String message, String id) {
        super(message);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
