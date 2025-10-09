package jr.chatbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "characters")
public class Character extends Resource {

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private String systemPrompt;

    private String shortGreeting;

    @Column(name = "profile_image_small")
    private String profileImageSmall;

    @Column(name = "profile_image_medium")
    private String profileImageMedium;

    @Column(name = "profile_image_large")
    private String profileImageLarge;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private Map<String, String> customFields = new HashMap<>();
}