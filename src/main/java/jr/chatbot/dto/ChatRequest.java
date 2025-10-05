package jr.chatbot.dto;

import jr.chatbot.entity.Message;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ChatRequest {
    private UUID ownerId;
    private UUID characterId;
    private List<Message> messageList;
}

