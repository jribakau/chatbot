package jr.chatbot.dto;

import jr.chatbot.entity.ChatMessage;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ChatRequest {
    private UUID characterId;
    private List<ChatMessage> history;
    private String userMessage;
}

