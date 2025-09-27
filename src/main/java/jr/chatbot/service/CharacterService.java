package jr.chatbot.service;

import jr.chatbot.entity.Character;
import jakarta.transaction.Transactional;
import jr.chatbot.repository.CharacterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CharacterService {
    private final CharacterRepository characterRepository;

    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public List<Character> getAllCharacters() {
        return characterRepository.findAll();
    }

    @Transactional
    public Optional<Character> getCharacterById(UUID id) {
        return characterRepository.findById(id);
    }

    public Character saveCharacter(Character character) {
        return characterRepository.save(character);
    }
}

