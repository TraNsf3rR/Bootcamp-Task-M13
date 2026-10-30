package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.config.AiProperties;
import com.accenture.springai_bootcamp_demo.entity.ChatMessage;
import com.accenture.springai_bootcamp_demo.entity.Role;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaChatClientTests {

    private static final String SYSTEM_PROMPT = "You are a concise assistant.";

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @Captor
    private ArgumentCaptor<List<Message>> messagesCaptor;

    private OllamaChatClient ollamaChatClient;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultSystem(SYSTEM_PROMPT)).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.messages(anyList())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);

        ollamaChatClient = new OllamaChatClient(chatClientBuilder, new AiProperties(SYSTEM_PROMPT));
    }

    @Test
    void sendsMappedConversationHistoryAndTrimsTheReply() {
        when(responseSpec.content()).thenReturn("  assistant reply  ");
        List<ChatMessage> history = List.of(
                ChatMessage.of(Role.SYSTEM, "system message"),
                ChatMessage.of(Role.USER, "first question"),
                ChatMessage.of(Role.ASSISTANT, "earlier answer"),
                ChatMessage.of(Role.USER, "follow-up question"));

        String reply = ollamaChatClient.complete(history);

        assertEquals("assistant reply", reply);
        verify(chatClientBuilder).defaultSystem(SYSTEM_PROMPT);
        verify(requestSpec).messages(messagesCaptor.capture());
        List<Message> messages = messagesCaptor.getValue();
        assertEquals(4, messages.size());
        assertInstanceOf(SystemMessage.class, messages.get(0));
        assertEquals("system message", messages.get(0).getText());
        assertInstanceOf(UserMessage.class, messages.get(1));
        assertEquals("first question", messages.get(1).getText());
        assertInstanceOf(AssistantMessage.class, messages.get(2));
        assertEquals("earlier answer", messages.get(2).getText());
        assertInstanceOf(UserMessage.class, messages.get(3));
        assertEquals("follow-up question", messages.get(3).getText());
    }

    @Test
    void rejectsAnEmptyOllamaResponse() {
        when(responseSpec.content()).thenReturn("  ");

        AiClientException exception = assertThrows(
                AiClientException.class,
                () -> ollamaChatClient.complete(List.of(ChatMessage.of(Role.USER, "question"))));

        assertEquals("Ollama returned an empty response", exception.getMessage());
    }

    @Test
    void summarizesHistoryWithATemporaryInstruction() {
        when(responseSpec.content()).thenReturn("Summary.");
        List<ChatMessage> history = new ArrayList<>(List.of(
                ChatMessage.of(Role.USER, "We decided on a June 1 deadline."),
                ChatMessage.of(Role.ASSISTANT, "Noted.")));

        String summary = ollamaChatClient.summarize(history);

        assertEquals("Summary.", summary);
        assertEquals(2, history.size());
        verify(requestSpec).messages(messagesCaptor.capture());
        List<Message> messages = messagesCaptor.getValue();
        assertEquals(3, messages.size());
        assertEquals("We decided on a June 1 deadline.", messages.get(0).getText());
        assertEquals("Noted.", messages.get(1).getText());
        assertInstanceOf(UserMessage.class, messages.get(2));
        assertTrue(messages.get(2).getText().contains("Include only details explicitly stated"));
    }

    @Test
    void wrapsFailuresWithOllamaContext() {
        RuntimeException cause = new RuntimeException("connection refused");
        when(responseSpec.content()).thenThrow(cause);

        AiClientException exception = assertThrows(
                AiClientException.class,
                () -> ollamaChatClient.complete(List.of(ChatMessage.of(Role.USER, "question"))));

        assertEquals("Failed to reach Ollama: connection refused", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
