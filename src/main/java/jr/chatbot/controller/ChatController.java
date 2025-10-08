package jr.chatbot.controller;

import jr.chatbot.dto.ChatRequest;
import jr.chatbot.entity.Chat;
import jr.chatbot.entity.Message;
import jr.chatbot.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController extends AbstractResourceController<Chat, ChatService> {

    public ChatController(ChatService chatService) {
        super(chatService);
    }

    @PostMapping("/new")
    public ResponseEntity<Chat> createChat(@RequestBody ChatRequest request) {
        UUID currentUserId = service.getCurrentUserIdOrThrow();

        Chat chat = new Chat();
        chat.setOwnerId(currentUserId);
        chat.setCharacterId(request.getCharacterId());

        if (request.getMessageList() != null && !request.getMessageList().isEmpty()) {
            for (Message message : request.getMessageList()) {
                message.setChat(chat);
                message.setOwnerId(currentUserId);
            }
            chat.getMessageList().addAll(request.getMessageList());
        }

        Chat savedChat = service.save(chat);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedChat);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Chat>> getAll() {
        throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "characterId parameter is required");
    }

    @GetMapping(params = "characterId")
    public ResponseEntity<List<Chat>> getChatsByCharacter(@RequestParam UUID characterId) {
        UUID currentUserId = service.getCurrentUserIdOrThrow();
        List<Chat> chats = service.findAllChatsByCharacterAndOwner(characterId, currentUserId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/latest")
    public ResponseEntity<Chat> getLatestChat(@RequestParam UUID characterId) {
        UUID currentUserId = service.getCurrentUserIdOrThrow();
        Chat chat = service.findLatestChatByCharacterAndOwnerWithMessages(characterId, currentUserId).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "No chat found"));
        return ResponseEntity.ok(chat);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Chat> getById(@PathVariable UUID id) {
        Chat chat = service.findChatByIdWithMessages(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));
        service.validateOwnership(chat);
        return ResponseEntity.ok(chat);
    }
}
