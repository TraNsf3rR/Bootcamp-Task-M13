package com.accenture.springai_bootcamp_demo.client;

/**
 * Raised when the configured AI provider cannot be reached or returns an
 * unusable response. Carries an HTTP-friendly status hint for the web layer.
 */
public class OpenRouterException extends RuntimeException {

    public OpenRouterException(String message) {
        super(message);
    }

    public OpenRouterException(String message, Throwable cause) {
        super(message, cause);
    }
}
