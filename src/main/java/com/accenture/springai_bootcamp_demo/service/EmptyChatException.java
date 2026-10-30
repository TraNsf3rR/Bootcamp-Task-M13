package com.accenture.springai_bootcamp_demo.service;

public class EmptyChatException extends RuntimeException {

    public EmptyChatException(String chatId) {
        super("Cannot summarize an empty chat: " + chatId);
    }
}
