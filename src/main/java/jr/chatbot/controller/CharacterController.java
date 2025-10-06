package jr.chatbot.controller;

import jr.chatbot.entity.Character;
import jr.chatbot.service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    @GetMapping("/characters")
    public List<Character> getCharacters() {
        return characterService.getAllCharacters();
    }

    @GetMapping("/characters/{id}")
    public ResponseEntity<Character> getCharacter(@PathVariable UUID id) {
        return characterService.getCharacterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/characters")
    public ResponseEntity<Character> createCharacter(@RequestBody Character character) {
        Character savedCharacter = characterService.saveCharacter(character);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCharacter);
    }

    @PutMapping("/characters/{id}")
    public ResponseEntity<Character> updateCharacter(@PathVariable UUID id, @RequestBody Character character) {
        return characterService.getCharacterById(id)
                .map(existingCharacter -> {
                    character.setId(id);
                    Character updatedCharacter = characterService.saveCharacter(character);
                    return ResponseEntity.ok(updatedCharacter);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/characters/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable UUID id) {
        boolean deleted = characterService.deleteCharacter(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}