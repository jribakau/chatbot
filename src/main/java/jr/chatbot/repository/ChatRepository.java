package jr.chatbot.repository;

import jr.chatbot.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("SELECT c FROM Chat c WHERE c.characterId = :characterId AND c.ownerId = :ownerId ORDER BY c.createdAt DESC LIMIT 1")
    Optional<Chat> findLatestByCharacterIdAndOwnerId(@Param("characterId") UUID characterId, @Param("ownerId") UUID ownerId);

    @Query("SELECT c FROM Chat c WHERE c.characterId = :characterId AND c.ownerId = :ownerId ORDER BY c.updatedAt DESC, c.createdAt DESC")
    List<Chat> findAllByCharacterIdAndOwnerId(@Param("characterId") UUID characterId, @Param("ownerId") UUID ownerId);
}
