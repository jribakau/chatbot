package jr.chatbot.entity;

import jr.chatbot.enums.MessageRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatMessage extends Resource {
    private MessageRoleEnum role;
    private String content;
    private ZonedDateTime timestamp;
}