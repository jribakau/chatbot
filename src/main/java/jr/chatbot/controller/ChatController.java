package jr.chatbot.controller;

import jr.chatbot.dto.ChatRequest;
import jr.chatbot.entity.ChatMessage;
import jr.chatbot.service.CharacterService;
import jr.chatbot.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatMessage> handleChat(@RequestBody ChatRequest chatRequest) {
        var character = characterService.getCharacterById(chatRequest.getCharacterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));

        ChatMessage aiResponse = chatService.getAIResponse(
                character,
                chatRequest.getHistory(),
                chatRequest.getUserMessage()
        );

        return ResponseEntity.ok(aiResponse);
    }
}
