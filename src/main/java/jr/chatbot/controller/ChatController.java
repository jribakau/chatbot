package jr.chatbot.controller;

import jr.chatbot.dto.ChatRequest;
import jr.chatbot.entity.Chat;
import jr.chatbot.entity.Message;
import jr.chatbot.service.ChatService;
import jr.chatbot.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<Chat> createChat(@RequestBody ChatRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

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

        Chat savedChat = chatService.saveChat(chat);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedChat);
    }

    @GetMapping("/chat")
    public ResponseEntity<List<Chat>> getChatsByCharacter(@RequestParam UUID characterId, @RequestParam(required = false) UUID ownerId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        List<Chat> chats = chatService.findAllChatsByCharacterAndOwner(characterId, currentUserId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/chat/latest")
    public ResponseEntity<Chat> getLatestChat(@RequestParam UUID characterId, @RequestParam(required = false) UUID ownerId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        Chat chat = chatService.findLatestChatByCharacterAndOwnerWithMessages(characterId, currentUserId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No chat found"));
        return ResponseEntity.ok(chat);
    }

    @GetMapping("/chat/{id}")
    public ResponseEntity<Chat> getChatById(@PathVariable UUID id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        Chat chat = chatService.findChatByIdWithMessages(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

        if (!chat.getOwnerId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok(chat);
    }
}
