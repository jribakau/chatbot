package jr.chatbot.repository;

import jr.chatbot.entity.Message;
import jr.chatbot.enums.ResourceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId")
    List<Message> findByChatId(@Param("chatId") UUID chatId);

    @Modifying
    @Query("UPDATE Message m SET m.resourceStatus = :status WHERE m.chat.id IN (SELECT c.id FROM Chat c WHERE c.characterId = :characterId)")
    int bulkUpdateResourceStatusByCharacterId(@Param("characterId") UUID characterId, @Param("status") ResourceStatusEnum status);
}
