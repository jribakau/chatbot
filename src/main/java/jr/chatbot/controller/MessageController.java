package jr.chatbot.controller;

import jr.chatbot.dto.MessageRequest;
import jr.chatbot.entity.Chat;
import jr.chatbot.entity.Message;
import jr.chatbot.enums.MessageRoleEnum;
import jr.chatbot.service.CharacterService;
import jr.chatbot.service.ChatService;
import jr.chatbot.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<Message> handleChat(@RequestBody MessageRequest messageRequest) {
        UUID currentUserId = chatService.getCurrentUserIdOrThrow();

        var character = characterService.findByIdOrThrow(messageRequest.getCharacterId());

        if (messageRequest.getChatId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chat ID is required");
        }

        Chat chat = chatService.findByIdOrThrow(messageRequest.getChatId());
        chatService.validateOwnership(chat);

        Message userMessage = new Message(MessageRoleEnum.USER, messageRequest.getUserMessage(), ZonedDateTime.now());
        userMessage.setOwnerId(currentUserId);
        chatService.addMessageToChat(messageRequest.getChatId(), userMessage);

        Message aiResponse = messageService.getAIResponse(character, messageRequest.getHistory(), messageRequest.getUserMessage());
        aiResponse.setOwnerId(currentUserId);

        chatService.addMessageToChat(messageRequest.getChatId(), aiResponse);

        return ResponseEntity.ok(aiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable UUID id, @RequestBody Message updatedMessage) {
        chatService.getCurrentUserIdOrThrow();

        Message existingMessage = messageService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        Chat chat = existingMessage.getChat();
        if (chat == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this message");
        }
        chatService.validateOwnership(chat);

        Message savedMessage = messageService.updateMessage(id, updatedMessage);
        return ResponseEntity.ok(savedMessage);
    }
}