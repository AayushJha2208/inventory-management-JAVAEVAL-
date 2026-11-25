package com.thapar.ims.exception;

/**
 * Our own checked exception.
 * The service throws it when input breaks a business rule
 * (empty name, negative quantity, missing product, bad date).
 * The screen catches it and shows the message to the user.
 */
public class InventoryException extends Exception {

    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
