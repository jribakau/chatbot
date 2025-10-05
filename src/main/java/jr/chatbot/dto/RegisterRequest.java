package jr.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    @JsonAlias({"password", "passwordHash"})
    private String password;
}
