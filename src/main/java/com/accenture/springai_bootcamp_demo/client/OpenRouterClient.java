package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.config.OpenRouterProperties;
import com.accenture.springai_bootcamp_demo.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Thin Spring AI client that accepts conversation history and returns the
 * assistant's reply text.
 */
@Slf4j
@Component
public class OpenRouterClient {
    private final ChatClient chatClient;

    public OpenRouterClient(ChatClient.Builder chatClientBuilder,
                            OpenRouterProperties properties) {
        this.chatClient = chatClientBuilder
                .defaultSystem(properties.systemPrompt())
                .build();
    }

    public String complete(List<ChatMessage> history) {
        String reply = call(history);

        return extractContent(reply);
    }

    private String call(List<ChatMessage> history) {
        try {
            return chatClient.prompt()
                    .messages(toMessages(history))
                    .call()
                    .content();
        } catch (OpenRouterException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Ollama request failed", ex);
            throw new OpenRouterException("Failed to reach Ollama: " + ex.getMessage(), ex);
        }
    }

    private List<Message> toMessages(List<ChatMessage> history) {
        return history.stream()
                .map(this::toMessage)
                .toList();
    }

    private Message toMessage(ChatMessage message) {
        return switch (message.getRole()) {
            case SYSTEM -> new SystemMessage(message.getContent());
            case USER -> new UserMessage(message.getContent());
            case ASSISTANT -> new AssistantMessage(message.getContent());
        };
    }

    private String extractContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new OpenRouterException("Ollama returned an empty response");
        }
        return content.trim();
    }
}
