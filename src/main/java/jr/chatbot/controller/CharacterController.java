package jr.chatbot.controller;

import jr.chatbot.entity.Character;
import jr.chatbot.service.CharacterService;
import jr.chatbot.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/characters")
public class CharacterController extends AbstractResourceController<Character, CharacterService> {

    @Autowired
    private ImageService imageService;

    public CharacterController(CharacterService characterService) {
        super(characterService);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Character>> getAll() {
        return ResponseEntity.ok(service.getAllCharacters());
    }

    @PostMapping("/{id}/profile-image")
    public ResponseEntity<Character> uploadProfileImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        Character character = service.findByIdOrThrow(id);
        service.validateOwnership(character);

        imageService.deleteCharacterImages(id);

        Map<String, String> imageUrls = imageService.uploadAndProcessImage(file, id);

        character.setProfileImageSmall(imageUrls.get("small"));
        character.setProfileImageMedium(imageUrls.get("medium"));
        character.setProfileImageLarge(imageUrls.get("large"));

        Character updatedCharacter = service.save(character);
        return ResponseEntity.ok(updatedCharacter);
    }

    @DeleteMapping("/{id}/profile-image")
    public ResponseEntity<Character> deleteProfileImage(@PathVariable UUID id) {
        Character character = service.findByIdOrThrow(id);
        service.validateOwnership(character);

        imageService.deleteCharacterImages(id);

        character.setProfileImageSmall(null);
        character.setProfileImageMedium(null);
        character.setProfileImageLarge(null);

        Character updatedCharacter = service.save(character);
        return ResponseEntity.ok(updatedCharacter);
    }
}