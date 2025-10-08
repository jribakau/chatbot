package jr.chatbot.service;

import jr.chatbot.entity.Chat;
import jr.chatbot.entity.Message;
import jr.chatbot.enums.MessageRoleEnum;
import jr.chatbot.enums.ResourceStatusEnum;
import jr.chatbot.repository.ChatRepository;
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

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ChatService chatService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private Chat testChat;
    private UUID testChatId;
    private UUID testCharacterId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testChatId = UUID.randomUUID();
        testCharacterId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testChat = new Chat();
        testChat.setId(testChatId);
        testChat.setCharacterId(testCharacterId);
        testChat.setOwnerId(testUserId);
        testChat.setResourceStatus(ResourceStatusEnum.ACTIVE);
        testChat.setMessageList(new ArrayList<>());

        securityUtilMock = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));

        // Act
        Optional<Chat> result = chatService.findById(testChatId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testChatId, result.get().getId());
        verify(chatRepository).findById(testChatId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        UUID randomId = UUID.randomUUID();
        when(chatRepository.findById(randomId)).thenReturn(Optional.empty());

        // Act
        Optional<Chat> result = chatService.findById(randomId);

        // Assert
        assertFalse(result.isPresent());
        verify(chatRepository).findById(randomId);
    }

    @Test
    void testFindChatByIdWithMessages_Success() {
        // Arrange
        Message msg1 = new Message(MessageRoleEnum.USER, "Hello", ZonedDateTime.now());
        Message msg2 = new Message(MessageRoleEnum.ASSISTANT, "Hi there!", ZonedDateTime.now());
        testChat.getMessageList().addAll(Arrays.asList(msg1, msg2));

        when(chatRepository.findByIdWithMessages(testChatId)).thenReturn(Optional.of(testChat));

        // Act
        Optional<Chat> result = chatService.findChatByIdWithMessages(testChatId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getMessageList().size());
        verify(chatRepository).findByIdWithMessages(testChatId);
    }

    @Test
    void testFindLatestChatByCharacterAndOwner_Success() {
        // Arrange
        when(chatRepository.findLatestByCharacterIdAndOwnerId(testCharacterId, testUserId)).thenReturn(Optional.of(testChat));

        // Act
        Optional<Chat> result = chatService.findLatestChatByCharacterAndOwner(testCharacterId, testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCharacterId, result.get().getCharacterId());
        assertEquals(testUserId, result.get().getOwnerId());
        verify(chatRepository).findLatestByCharacterIdAndOwnerId(testCharacterId, testUserId);
    }

    @Test
    void testFindLatestChatByCharacterAndOwner_NotFound() {
        // Arrange
        when(chatRepository.findLatestByCharacterIdAndOwnerId(testCharacterId, testUserId)).thenReturn(Optional.empty());

        // Act
        Optional<Chat> result = chatService.findLatestChatByCharacterAndOwner(testCharacterId, testUserId);

        // Assert
        assertFalse(result.isPresent());
        verify(chatRepository).findLatestByCharacterIdAndOwnerId(testCharacterId, testUserId);
    }

    @Test
    void testFindLatestChatByCharacterAndOwnerWithMessages_Success() {
        // Arrange
        Message msg = new Message(MessageRoleEnum.USER, "Test message", ZonedDateTime.now());
        testChat.getMessageList().add(msg);

        when(chatRepository.findLatestByCharacterIdAndOwnerIdWithMessages(testCharacterId, testUserId)).thenReturn(Optional.of(testChat));

        // Act
        Optional<Chat> result = chatService.findLatestChatByCharacterAndOwnerWithMessages(testCharacterId, testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getMessageList().size());
        verify(chatRepository).findLatestByCharacterIdAndOwnerIdWithMessages(testCharacterId, testUserId);
    }

    @Test
    void testFindAllChatsByCharacterAndOwner_Success() {
        // Arrange
        Chat chat1 = new Chat();
        chat1.setId(UUID.randomUUID());
        chat1.setCharacterId(testCharacterId);
        chat1.setOwnerId(testUserId);

        Chat chat2 = new Chat();
        chat2.setId(UUID.randomUUID());
        chat2.setCharacterId(testCharacterId);
        chat2.setOwnerId(testUserId);

        List<Chat> chats = Arrays.asList(chat1, chat2);

        when(chatRepository.findAllByCharacterIdAndOwnerId(testCharacterId, testUserId)).thenReturn(chats);

        // Act
        List<Chat> result = chatService.findAllChatsByCharacterAndOwner(testCharacterId, testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(chatRepository).findAllByCharacterIdAndOwnerId(testCharacterId, testUserId);
    }

    @Test
    void testFindAllChatsByCharacterAndOwnerWithMessages_Success() {
        // Arrange
        Chat chat1 = new Chat();
        chat1.setId(UUID.randomUUID());
        chat1.setMessageList(new ArrayList<>());

        List<Chat> chats = List.of(chat1);

        when(chatRepository.findAllByCharacterIdAndOwnerIdWithMessages(testCharacterId, testUserId)).thenReturn(chats);

        // Act
        List<Chat> result = chatService.findAllChatsByCharacterAndOwnerWithMessages(testCharacterId, testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(chatRepository).findAllByCharacterIdAndOwnerIdWithMessages(testCharacterId, testUserId);
    }

    @Test
    void testAddMessageToChat_Success() {
        // Arrange
        Message newMessage = new Message(MessageRoleEnum.USER, "New message", ZonedDateTime.now());

        when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));
        when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

        // Act
        chatService.addMessageToChat(testChatId, newMessage);

        // Assert
        assertEquals(testChat, newMessage.getChat());
        assertEquals(1, testChat.getMessageList().size());
        assertTrue(testChat.getMessageList().contains(newMessage));
        verify(chatRepository).findById(testChatId);
        verify(chatRepository).save(testChat);
    }

    @Test
    void testAddMessageToChat_ChatNotFound_ThrowsException() {
        // Arrange
        UUID randomChatId = UUID.randomUUID();
        Message newMessage = new Message(MessageRoleEnum.USER, "New message", ZonedDateTime.now());

        when(chatRepository.findById(randomChatId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> chatService.addMessageToChat(randomChatId, newMessage));

        verify(chatRepository).findById(randomChatId);
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void testSoftDeleteChatsByCharacterId_Success() {
        // Arrange
        int deletedCount = 3;
        when(chatRepository.bulkUpdateResourceStatusByCharacterId(testCharacterId, ResourceStatusEnum.DELETED)).thenReturn(deletedCount);

        // Act
        int result = chatService.softDeleteChatsByCharacterId(testCharacterId);

        // Assert
        assertEquals(deletedCount, result);
        verify(chatRepository).bulkUpdateResourceStatusByCharacterId(testCharacterId, ResourceStatusEnum.DELETED);
    }

    @Test
    void testFindAllChatsByCharacterId_Success() {
        // Arrange
        Chat chat1 = new Chat();
        chat1.setId(UUID.randomUUID());
        chat1.setCharacterId(testCharacterId);

        Chat chat2 = new Chat();
        chat2.setId(UUID.randomUUID());
        chat2.setCharacterId(testCharacterId);

        List<Chat> chats = Arrays.asList(chat1, chat2);

        when(chatRepository.findByCharacterId(testCharacterId)).thenReturn(chats);

        // Act
        List<Chat> result = chatService.findAllChatsByCharacterId(testCharacterId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getCharacterId().equals(testCharacterId)));
        verify(chatRepository).findByCharacterId(testCharacterId);
    }

    @Test
    void testSave_NewChat_SetsOwnerId() {
        // Arrange
        Chat newChat = new Chat();
        newChat.setCharacterId(testCharacterId);

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUserId);
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> {
            Chat saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        Chat result = chatService.save(newChat);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getOwnerId());
        verify(chatRepository).save(newChat);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        Chat updatedChat = new Chat();
        updatedChat.setCharacterId(UUID.randomUUID());

        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUserId);
        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Chat result = chatService.update(testChatId, updatedChat);

        // Assert
        assertNotNull(result);
        assertEquals(testChatId, result.getId());
        assertEquals(testUserId, result.getOwnerId());
        verify(chatRepository).findById(testChatId);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testSoftDelete_Success() {
        // Arrange
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUserId);
        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));
        when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

        // Act
        boolean result = chatService.softDelete(testChatId);

        // Assert
        assertTrue(result);
        assertEquals(ResourceStatusEnum.DELETED, testChat.getResourceStatus());
        verify(chatRepository).findById(testChatId);
        verify(chatRepository).save(testChat);
    }

    @Test
    void testHardDelete_Success() {
        // Arrange
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(testUserId);
        securityUtilMock.when(SecurityUtil::isCurrentUserAdmin).thenReturn(false);
        when(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat));
        doNothing().when(chatRepository).delete(testChat);

        // Act
        chatService.hardDelete(testChatId);

        // Assert
        verify(chatRepository).findById(testChatId);
        verify(chatRepository).delete(testChat);
    }
}

