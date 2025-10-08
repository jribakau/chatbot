package jr.chatbot.service;

import jakarta.transaction.Transactional;
import jr.chatbot.entity.Character;
import jr.chatbot.enums.ResourceStatusEnum;
import jr.chatbot.repository.CharacterRepository;
import jr.chatbot.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CharacterService extends AbstractResourceService<Character, CharacterRepository> {
    private final ChatService chatService;
    private final MessageService messageService;

    public CharacterService(CharacterRepository characterRepository, ChatService chatService, MessageService messageService) {
        super(characterRepository);
        this.chatService = chatService;
        this.messageService = messageService;
    }

    public List<Character> getAllCharacters() {
        if (SecurityUtil.isCurrentUserAdmin()) {
            return repository.findByResourceStatus(ResourceStatusEnum.ACTIVE);
        }
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        return repository.findByResourceStatusAndOwnerId(ResourceStatusEnum.ACTIVE, currentUserId);
    }

    @Override
    @Transactional
    public boolean softDelete(UUID id) {
        Character character = findByIdOrThrow(id);
        validateOwnership(character);

        character.setResourceStatus(ResourceStatusEnum.DELETED);
        repository.save(character);

        messageService.softDeleteMessagesByCharacterId(id);
        chatService.softDeleteChatsByCharacterId(id);

        return true;
    }
}
