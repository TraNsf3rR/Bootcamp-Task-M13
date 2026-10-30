package com.accenture.springai_bootcamp_demo.service;

import com.accenture.springai_bootcamp_demo.client.OllamaChatClient;
import com.accenture.springai_bootcamp_demo.dto.ChatSummaryResponse;
import com.accenture.springai_bootcamp_demo.entity.Chat;
import com.accenture.springai_bootcamp_demo.entity.ChatMessage;
import com.accenture.springai_bootcamp_demo.entity.Role;
import com.accenture.springai_bootcamp_demo.mapper.ChatMapper;
import com.accenture.springai_bootcamp_demo.repository.ChatRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTests {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private OllamaChatClient ollamaChatClient;

    @Mock
    private ChatMapper chatMapper;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(chatRepository, ollamaChatClient, chatMapper);
    }

    @Test
    void returnsSummaryWithoutChangingChatHistory() {
        Chat chat = chatWithMessages();
        when(chatRepository.findWithMessagesById(chat.getId())).thenReturn(Optional.of(chat));
        when(ollamaChatClient.summarize(chat.getChatMessages())).thenReturn("Summary.");

        ChatSummaryResponse response = chatService.summarizeChat(chat.getId());

        assertEquals(chat.getId(), response.chatId());
        assertEquals("Summary.", response.summary());
        assertEquals(2, chat.getChatMessages().size());
        verify(ollamaChatClient).summarize(chat.getChatMessages());
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void rejectsAnEmptyChatWithoutCallingOllama() {
        Chat chat = Chat.create("Empty chat");
        when(chatRepository.findWithMessagesById(chat.getId())).thenReturn(Optional.of(chat));

        assertThrows(EmptyChatException.class, () -> chatService.summarizeChat(chat.getId()));

        verifyNoInteractions(ollamaChatClient);
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void preservesNotFoundBehaviorForUnknownChats() {
        when(chatRepository.findWithMessagesById("missing")).thenReturn(Optional.empty());

        assertThrows(ChatNotFoundException.class, () -> chatService.summarizeChat("missing"));

        verifyNoInteractions(ollamaChatClient);
    }

    private Chat chatWithMessages() {
        Chat chat = Chat.create("Planning");
        chat.addMessage(ChatMessage.of(Role.USER, "We decided on June 1."));
        chat.addMessage(ChatMessage.of(Role.ASSISTANT, "Noted."));
        return chat;
    }
}
