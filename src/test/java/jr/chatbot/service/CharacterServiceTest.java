package jr.chatbot.service;

import jr.chatbot.entity.Character;
import jr.chatbot.enums.ResourceStatusEnum;
import jr.chatbot.repository.CharacterRepository;
import jr.chatbot.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private ChatService chatService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private CharacterService characterService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private Character testCharacter;
    private UUID testCharacterId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testCharacterId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testCharacter = new Character();
        testCharacter.setId(testCharacterId);
        testCharacter.setName("Test Character");
        testCharacter.setDescription("A test character");
        testCharacter.setSystemPrompt("You are a helpful assistant");
        testCharacter.setOwnerId(testUserId);
        testCharacter.setResourceStatus(ResourceStatusEnum.ACTIVE);

        securityUtilMock = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    void testGetAllCharacters_AsAdmin() {
        // Arrange
        Character char1 = new Character();
        char1.setId(UUID.randomUUID());
        char1.setName("Character 1");
        char1.setResourceStatus(ResourceStatusEnum.ACTIVE);

        Character char2 = new Character();
        char2.setId(UUID.randomUUID());
        char2.setName("Character 2");
        char2.setResourceStatus(ResourceStatusEnum.ACTIVE);

        List<Character> characters = Arrays.asList(char1, char2);

        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(true);
        when(characterRepository.findByResourceStatus(ResourceStatusEnum.ACTIVE)).thenReturn(characters);

        // Act
        List<Character> result = characterService.getAllCharacters();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(characterRepository).findByResourceStatus(ResourceStatusEnum.ACTIVE);
        verify(characterRepository, never()).findByResourceStatusAndOwnerId(any(), any());
    }

    @Test
    void testGetAllCharacters_AsRegularUser() {
        // Arrange
        Character char1 = new Character();
        char1.setId(UUID.randomUUID());
        char1.setName("My Character");
        char1.setOwnerId(testUserId);
        char1.setResourceStatus(ResourceStatusEnum.ACTIVE);

        List<Character> characters = List.of(char1);

        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUserId);
        when(characterRepository.findByResourceStatusAndOwnerId(ResourceStatusEnum.ACTIVE, testUserId))
                .thenReturn(characters);

        // Act
        List<Character> result = characterService.getAllCharacters();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserId, result.getFirst().getOwnerId());
        verify(characterRepository).findByResourceStatusAndOwnerId(ResourceStatusEnum.ACTIVE, testUserId);
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(characterRepository.findById(testCharacterId)).thenReturn(Optional.of(testCharacter));

        // Act
        Optional<Character> result = characterService.findById(testCharacterId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCharacterId, result.get().getId());
        verify(characterRepository).findById(testCharacterId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        UUID randomId = UUID.randomUUID();
        when(characterRepository.findById(randomId)).thenReturn(Optional.empty());

        // Act
        Optional<Character> result = characterService.findById(randomId);

        // Assert
        assertFalse(result.isPresent());
        verify(characterRepository).findById(randomId);
    }

    @Test
    void testFindByIdOrThrow_Success() {
        // Arrange
        when(characterRepository.findById(testCharacterId)).thenReturn(Optional.of(testCharacter));

        // Act
        Character result = characterService.findByIdOrThrow(testCharacterId);

        // Assert
        assertNotNull(result);
        assertEquals(testCharacterId, result.getId());
        verify(characterRepository).findById(testCharacterId);
    }

    @Test
    void testFindByIdOrThrow_NotFound_ThrowsException() {
        // Arrange
        UUID randomId = UUID.randomUUID();
        when(characterRepository.findById(randomId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                characterService.findByIdOrThrow(randomId)
        );

        assertTrue(exception.getMessage().contains("Character not found"));
        verify(characterRepository).findById(randomId);
    }

    @Test
    void testSave_NewCharacter_SetsOwnerId() {
        // Arrange
        Character newCharacter = new Character();
        newCharacter.setName("New Character");

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUserId);
        when(characterRepository.save(any(Character.class))).thenAnswer(invocation -> {
            Character saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        Character result = characterService.save(newCharacter);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getOwnerId());
        verify(characterRepository).save(newCharacter);
    }

    @Test
    void testSave_ExistingCharacter_KeepsOwnerId() {
        // Arrange
        testCharacter.setId(testCharacterId);
        testCharacter.setOwnerId(testUserId);

        when(characterRepository.save(testCharacter)).thenReturn(testCharacter);

        // Act
        Character result = characterService.save(testCharacter);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getOwnerId());
        verify(characterRepository).save(testCharacter);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        Character updatedCharacter = new Character();
        updatedCharacter.setName("Updated Character");
        updatedCharacter.setDescription("Updated description");

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUserId);
        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        when(characterRepository.findById(testCharacterId)).thenReturn(Optional.of(testCharacter));
        when(characterRepository.save(any(Character.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Character result = characterService.update(testCharacterId, updatedCharacter);

        // Assert
        assertNotNull(result);
        assertEquals(testCharacterId, result.getId());
        assertEquals(testUserId, result.getOwnerId());
        assertEquals("Updated Character", result.getName());
        verify(characterRepository).findById(testCharacterId);
        verify(characterRepository).save(any(Character.class));
    }

    @Test
    void testUpdate_NotOwner_ThrowsException() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        Character updatedCharacter = new Character();
        updatedCharacter.setName("Updated Character");

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(differentUserId);
        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        when(characterRepository.findById(testCharacterId)).thenReturn(Optional.of(testCharacter));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                characterService.update(testCharacterId, updatedCharacter)
        );

        assertTrue(exception.getMessage().contains("Access denied"));
        verify(characterRepository).findById(testCharacterId);
        verify(characterRepository, never()).save(any(Character.class));
    }

    @Test
    void testSoftDelete_Success() {
        // Arrange
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUserId);
        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        when(characterRepository.findById(testCharacterId)).thenReturn(Optional.of(testCharacter));
        when(characterRepository.save(any(Character.class))).thenReturn(testCharacter);
        when(messageService.softDeleteMessagesByCharacterId(testCharacterId)).thenReturn(5);
        when(chatService.softDeleteChatsByCharacterId(testCharacterId)).thenReturn(2);

        // Act
        boolean result = characterService.softDelete(testCharacterId);

        // Assert
        assertTrue(result);
        assertEquals(ResourceStatusEnum.DELETED, testCharacter.getResourceStatus());
        verify(characterRepository).findById(testCharacterId);
        verify(characterRepository).save(testCharacter);
        verify(messageService).softDeleteMessagesByCharacterId(testCharacterId);
        verify(chatService).softDeleteChatsByCharacterId(testCharacterId);
    }

    @Test
    void testSoftDelete_NotOwner_ThrowsException() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(differentUserId);
        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        when(characterRepository.findById(testCharacterId)).thenReturn(Optional.of(testCharacter));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                characterService.softDelete(testCharacterId)
        );

        assertTrue(exception.getMessage().contains("Access denied"));
        verify(characterRepository).findById(testCharacterId);
        verify(characterRepository, never()).save(any(Character.class));
        verify(messageService, never()).softDeleteMessagesByCharacterId(any());
        verify(chatService, never()).softDeleteChatsByCharacterId(any());
    }

    @Test
    void testHardDelete_Success() {
        // Arrange
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUserId);
        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        when(characterRepository.findById(testCharacterId)).thenReturn(Optional.of(testCharacter));
        doNothing().when(characterRepository).delete(testCharacter);

        // Act
        characterService.hardDelete(testCharacterId);

        // Assert
        verify(characterRepository).findById(testCharacterId);
        verify(characterRepository).delete(testCharacter);
    }

    @Test
    void testHardDelete_NotOwner_ThrowsException() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(differentUserId);
        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        when(characterRepository.findById(testCharacterId)).thenReturn(Optional.of(testCharacter));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                characterService.hardDelete(testCharacterId)
        );

        assertTrue(exception.getMessage().contains("Access denied"));
        verify(characterRepository).findById(testCharacterId);
        verify(characterRepository, never()).delete(any(Character.class));
    }
}
