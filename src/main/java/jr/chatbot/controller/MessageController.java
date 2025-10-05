package jr.chatbot.controller;

import jr.chatbot.dto.MessageRequest;
import jr.chatbot.entity.Chat;
import jr.chatbot.entity.Message;
import jr.chatbot.enums.MessageRoleEnum;
import jr.chatbot.service.CharacterService;
import jr.chatbot.service.ChatService;
import jr.chatbot.service.MessageService;
import jr.chatbot.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<Message> handleChat(@RequestBody MessageRequest messageRequest) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        var character = characterService.getCharacterById(messageRequest.getCharacterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));

        if (messageRequest.getChatId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chat ID is required");
        }

        Chat chat = chatService.findChatById(messageRequest.getChatId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

        if (!chat.getOwnerId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this chat");
        }

        Message userMessage = new Message(MessageRoleEnum.USER, messageRequest.getUserMessage(), ZonedDateTime.now());
        userMessage.setOwnerId(currentUserId);
        chatService.addMessageToChat(messageRequest.getChatId(), userMessage);

        Message aiResponse = messageService.getAIResponse(character, messageRequest.getHistory(), messageRequest.getUserMessage());
        aiResponse.setOwnerId(currentUserId);

        chatService.addMessageToChat(messageRequest.getChatId(), aiResponse);

        return ResponseEntity.ok(aiResponse);
    }

    @PutMapping("/message/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable UUID id, @RequestBody Message updatedMessage) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        Message existingMessage = messageService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        Chat chat = existingMessage.getChat();
        if (chat == null || !chat.getOwnerId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this message");
        }

        Message savedMessage = messageService.updateMessage(id, updatedMessage);
        return ResponseEntity.ok(savedMessage);
    }
}