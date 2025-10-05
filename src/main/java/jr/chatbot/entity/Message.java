package jr.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
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
@Entity
@Table(name = "messages")
public class Message extends Resource {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    @JsonBackReference
    private Chat chat;
    @Column
    private MessageRoleEnum role;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Column
    private ZonedDateTime timestamp;

    public Message(MessageRoleEnum role, String content, ZonedDateTime timestamp) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }
}