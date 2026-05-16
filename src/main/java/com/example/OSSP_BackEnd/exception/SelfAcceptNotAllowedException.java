package com.example.OSSP_BackEnd.exception;

public class SelfAcceptNotAllowedException extends RuntimeException {

    public SelfAcceptNotAllowedException(String message) {
        super(message);
    }
}