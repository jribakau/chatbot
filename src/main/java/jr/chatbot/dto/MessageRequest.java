package jr.chatbot.dto;

import jr.chatbot.entity.Message;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class MessageRequest {
    private UUID characterId;
    private List<Message> history;
    private String userMessage;
}