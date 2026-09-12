package com.erydevs.addon.exception;

public class AddonLoadException extends Exception {

    public AddonLoadException(String message) {
        super(message);
    }

    public AddonLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
