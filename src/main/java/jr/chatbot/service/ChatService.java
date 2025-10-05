package jr.chatbot.service;

import jr.chatbot.entity.Chat;
import jr.chatbot.entity.Message;
import jr.chatbot.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat saveChat(Chat chat) {
        return chatRepository.save(chat);
    }

    public Optional<Chat> findChatById(UUID chatId) {
        return chatRepository.findById(chatId);
    }

    public Optional<Chat> findChatByIdWithMessages(UUID chatId) {
        return chatRepository.findByIdWithMessages(chatId);
    }

    public Optional<Chat> findLatestChatByCharacterAndOwner(UUID characterId, UUID ownerId) {
        return chatRepository.findLatestByCharacterIdAndOwnerId(characterId, ownerId);
    }

    public Optional<Chat> findLatestChatByCharacterAndOwnerWithMessages(UUID characterId, UUID ownerId) {
        return chatRepository.findLatestByCharacterIdAndOwnerIdWithMessages(characterId, ownerId);
    }

    public List<Chat> findAllChatsByCharacterAndOwner(UUID characterId, UUID ownerId) {
        return chatRepository.findAllByCharacterIdAndOwnerId(characterId, ownerId);
    }

    public List<Chat> findAllChatsByCharacterAndOwnerWithMessages(UUID characterId, UUID ownerId) {
        return chatRepository.findAllByCharacterIdAndOwnerIdWithMessages(characterId, ownerId);
    }

    public void addMessageToChat(UUID chatId, Message message) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        message.setChat(chat);
        chat.getMessageList().add(message);

        chatRepository.save(chat);
    }
}
