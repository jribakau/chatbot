package jr.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private UUID userId;
    private String username;
    private String email;
    private String message;
}
