package com.athlete.exception;

/**
 * Created by santiago on 02/11/13.
 */
public class InvalidActivityTypeException extends Exception {
    public InvalidActivityTypeException(String detailMessage) {
        super(detailMessage);
    }
}
