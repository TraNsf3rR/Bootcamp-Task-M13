package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.config.AiProperties;
import com.accenture.springai_bootcamp_demo.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin Spring AI client that accepts conversation history and returns the
 * assistant's reply text.
 */
@Slf4j
@Component
public class OllamaChatClient {
    private static final String SUMMARY_INSTRUCTION =
            "Summarize the conversation above in 3-5 concise bullet points using an ASCII hyphen (-). "
                    + "Include only details explicitly stated in the conversation. "
                    + "Distinguish user-stated facts or decisions from assistant suggestions. "
                    + "Do not infer missing details or add recommendations.";

    private final ChatClient chatClient;

    public OllamaChatClient(ChatClient.Builder chatClientBuilder,
                            AiProperties properties) {
        this.chatClient = chatClientBuilder
                .defaultSystem(properties.systemPrompt())
                .build();
    }

    public String complete(List<ChatMessage> history) {
        return extractContent(call(toMessages(history)));
    }

    public String summarize(List<ChatMessage> history) {
        List<Message> promptMessages = new ArrayList<>(toMessages(history));
        promptMessages.add(new UserMessage(SUMMARY_INSTRUCTION));
        return extractContent(call(promptMessages));
    }

    private String call(List<Message> messages) {
        try {
            return chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();
        } catch (AiClientException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Ollama request failed", ex);
            throw new AiClientException("Failed to reach Ollama: " + ex.getMessage(), ex);
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
            throw new AiClientException("Ollama returned an empty response");
        }
        return content.trim();
    }
}
