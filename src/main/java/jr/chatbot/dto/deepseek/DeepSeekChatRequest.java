package jr.chatbot.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import jr.chatbot.entity.ChatMessage;
import lombok.Data;

import java.util.List;

@Data
public class DeepSeekChatRequest {
    private String model;
    private List<ChatMessage> messages;
    @JsonProperty("max_tokens")
    private int maxTokens = 8192;
    private double temperature = 1;
}
