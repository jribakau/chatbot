package jr.chatbot.service;

import jr.chatbot.entity.Chat;
import jr.chatbot.repository.ChatRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat saveChat(Chat chat) {
        return chatRepository.save(chat);
    }
}
