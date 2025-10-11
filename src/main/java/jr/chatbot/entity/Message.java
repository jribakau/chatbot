package jr.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jr.chatbot.enums.MessageRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_message_chat_id", columnList = "chat_id"),
    @Index(name = "idx_message_timestamp", columnList = "timestamp")
})
public class Message extends Resource {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    @JsonBackReference
    private Chat chat;
    @Column
    private MessageRoleEnum role;
    @Column(length = 10000)
    private String content;
    @Column
    private ZonedDateTime timestamp;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "message_versions", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "version_content", length = 10000)
    private List<String> versions = new ArrayList<>();

    @Column(name = "current_version_index")
    private Integer currentVersionIndex;

    public Message(MessageRoleEnum role, String content, ZonedDateTime timestamp) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }
}