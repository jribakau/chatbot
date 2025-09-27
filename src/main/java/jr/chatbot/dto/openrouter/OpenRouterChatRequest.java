package jr.chatbot.dto.openrouter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenRouterChatRequest {
    private String model;
    private List<OpenAIMessage> messages;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;
}

