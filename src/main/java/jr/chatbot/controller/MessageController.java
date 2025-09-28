package jr.chatbot.controller;

import jr.chatbot.dto.MessageRequest;
import jr.chatbot.entity.Message;
import jr.chatbot.service.CharacterService;
import jr.chatbot.service.MessageService;
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
public class MessageController {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private MessageService messageService;

    @PostMapping("/message")
    public ResponseEntity<Message> handleChat(@RequestBody MessageRequest messageRequest) {
        var character = characterService.getCharacterById(messageRequest.getCharacterId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));

        Message aiResponse = messageService.getAIResponse(character, messageRequest.getHistory(), messageRequest.getUserMessage());

        return ResponseEntity.ok(aiResponse);
    }
}