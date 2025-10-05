package jr.chatbot.controller;

import jr.chatbot.dto.ChatRequest;
import jr.chatbot.entity.Chat;
import jr.chatbot.entity.Message;
import jr.chatbot.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<Chat> createChat(@RequestBody ChatRequest request) {
        Chat chat = new Chat();
        chat.setOwnerId(request.getOwnerId());
        chat.setCharacterId(request.getCharacterId());

        if (request.getMessageList() != null && !request.getMessageList().isEmpty()) {
            for (Message message : request.getMessageList()) {
                message.setChat(chat);
            }
            chat.getMessageList().addAll(request.getMessageList());
        }

        Chat savedChat = chatService.saveChat(chat);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedChat);
    }

    @GetMapping("/chat/latest")
    public ResponseEntity<Chat> getLatestChat(
            @RequestParam UUID characterId,
            @RequestParam UUID ownerId) {
        Chat chat = chatService.findLatestChatByCharacterAndOwner(characterId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No chat found"));
        return ResponseEntity.ok(chat);
    }

    @GetMapping("/chat/{id}")
    public ResponseEntity<Chat> getChatById(@PathVariable UUID id) {
        Chat chat = chatService.findChatById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));
        return ResponseEntity.ok(chat);
    }
}
