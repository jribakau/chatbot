package jr.chatbot.service;

import jakarta.transaction.Transactional;
import jr.chatbot.entity.Character;
import jr.chatbot.entity.Chat;
import jr.chatbot.enums.ResourceStatusEnum;
import jr.chatbot.repository.CharacterRepository;
import jr.chatbot.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CharacterService {
    private final CharacterRepository characterRepository;
    private final ChatService chatService;
    private final MessageService messageService;

    public CharacterService(CharacterRepository characterRepository, ChatService chatService, MessageService messageService) {
        this.characterRepository = characterRepository;
        this.chatService = chatService;
        this.messageService = messageService;
    }

    public List<Character> getAllCharacters() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        return characterRepository.findByResourceStatusAndOwnerId(ResourceStatusEnum.ACTIVE, currentUserId);
    }

    @Transactional
    public Optional<Character> getCharacterById(UUID id) {
        return characterRepository.findById(id);
    }

    public Character saveCharacter(Character character) {
        if (character.getId() == null && character.getOwnerId() == null) {
            UUID currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId != null) {
                character.setOwnerId(currentUserId);
            }
        }
        return characterRepository.save(character);
    }

    @Transactional
    public boolean deleteCharacter(UUID id) {
        Optional<Character> characterOpt = characterRepository.findById(id);
        if (characterOpt.isPresent()) {
            Character character = characterOpt.get();
            character.setResourceStatus(ResourceStatusEnum.DELETED);
            characterRepository.save(character);

            List<Chat> chats = chatService.findAllChatsByCharacterId(id);
            for (Chat chat : chats) {
                messageService.softDeleteMessagesByChatId(chat.getId());
            }
            chatService.softDeleteChatsByCharacterId(id);

            return true;
        }
        return false;
    }
}
