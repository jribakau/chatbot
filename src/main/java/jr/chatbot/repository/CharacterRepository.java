package jr.chatbot.repository;

import jr.chatbot.entity.Character;
import jr.chatbot.enums.ResourceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CharacterRepository extends JpaRepository<Character, UUID> {
    Optional<Character> findByName(String name);
    List<Character> findByResourceStatus(ResourceStatusEnum resourceStatus);
}
