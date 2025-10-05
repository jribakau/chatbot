package jr.chatbot.service;

import jr.chatbot.entity.Chat;
import jr.chatbot.entity.Message;
import jr.chatbot.repository.ChatRepository;
import org.springframework.stereotype.Service;

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

    public Optional<Chat> findLatestChatByCharacterAndOwner(UUID characterId, UUID ownerId) {
        return chatRepository.findLatestByCharacterIdAndOwnerId(characterId, ownerId);
    }

    public Chat addMessageToChat(UUID chatId, Message message) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        message.setChat(chat);
        chat.getMessageList().add(message);

        return chatRepository.save(chat);
    }
}
