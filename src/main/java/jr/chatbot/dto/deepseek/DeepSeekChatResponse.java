package jr.chatbot.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import jr.chatbot.entity.ChatMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DeepSeekChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    @JsonProperty("usage")
    private Usage usage;
    private List<Choice> choices;

    @Data
    @NoArgsConstructor
    public static class Choice {
        private int index;
        private ChatMessage message;
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;
    }
}

