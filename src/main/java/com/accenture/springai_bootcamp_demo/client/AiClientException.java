package com.accenture.springai_bootcamp_demo.client;

/**
 * Raised when the configured AI provider cannot be reached or returns an
 * unusable response. Carries an HTTP-friendly status hint for the web layer.
 */
public class AiClientException extends RuntimeException {

    public AiClientException(String message) {
        super(message);
    }

    public AiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
