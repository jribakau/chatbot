package jr.chatbot.repository;

import jr.chatbot.entity.Character;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterRepository extends ResourceRepository<Character> {
    Optional<Character> findByName(String name);
}
