package jr.chatbot.service;

import jakarta.transaction.Transactional;
import jr.chatbot.entity.Chat;
import jr.chatbot.entity.Message;
import jr.chatbot.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService extends AbstractResourceService<Chat, ChatRepository> {

    public ChatService(ChatRepository chatRepository) {
        super(chatRepository);
    }

    public Optional<Chat> findChatByIdWithMessages(UUID chatId) {
        return repository.findByIdWithMessages(chatId);
    }

    public Optional<Chat> findLatestChatByCharacterAndOwner(UUID characterId, UUID ownerId) {
        return repository.findLatestByCharacterIdAndOwnerId(characterId, ownerId);
    }

    public Optional<Chat> findLatestChatByCharacterAndOwnerWithMessages(UUID characterId, UUID ownerId) {
        return repository.findLatestByCharacterIdAndOwnerIdWithMessages(characterId, ownerId);
    }

    public List<Chat> findAllChatsByCharacterAndOwner(UUID characterId, UUID ownerId) {
        return repository.findAllByCharacterIdAndOwnerId(characterId, ownerId);
    }

    public List<Chat> findAllChatsByCharacterAndOwnerWithMessages(UUID characterId, UUID ownerId) {
        return repository.findAllByCharacterIdAndOwnerIdWithMessages(characterId, ownerId);
    }

    public void addMessageToChat(UUID chatId, Message message) {
        Chat chat = findByIdOrThrow(chatId);
        message.setChat(chat);
        chat.getMessageList().add(message);
        repository.save(chat);
    }

    @Transactional
    public int softDeleteChatsByCharacterId(UUID characterId) {
        return repository.bulkUpdateResourceStatusByCharacterId(characterId, jr.chatbot.enums.ResourceStatusEnum.DELETED);
    }

    public List<Chat> findAllChatsByCharacterId(UUID characterId) {
        return repository.findByCharacterId(characterId);
    }
}
