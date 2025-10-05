package jr.chatbot.repository;

import jr.chatbot.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("SELECT c FROM Chat c WHERE c.characterId = :characterId AND c.ownerId = :ownerId ORDER BY c.createdAt DESC")
    List<Chat> findLatestByCharacterIdAndOwnerId(@Param("characterId") UUID characterId, @Param("ownerId") UUID ownerId, Pageable pageable);
}
