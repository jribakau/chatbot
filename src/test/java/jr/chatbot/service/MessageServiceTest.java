package jr.chatbot.service;

import jr.chatbot.dto.openrouter.OpenAIMessage;
import jr.chatbot.dto.openrouter.OpenRouterChatResponse;
import jr.chatbot.entity.Character;
import jr.chatbot.entity.Message;
import jr.chatbot.enums.MessageRoleEnum;
import jr.chatbot.enums.ResourceStatusEnum;
import jr.chatbot.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private RestTemplate restTemplate;

    private MessageService messageService;

    private UUID testMessageId;
    private UUID testChatId;
    private UUID testCharacterId;
    private Message testMessage;
    private Character testCharacter;

    @BeforeEach
    void setUp() {
        testMessageId = UUID.randomUUID();
        testChatId = UUID.randomUUID();
        testCharacterId = UUID.randomUUID();

        testMessage = new Message();
        testMessage.setId(testMessageId);
        testMessage.setRole(MessageRoleEnum.USER);
        testMessage.setContent("Test message");
        testMessage.setTimestamp(ZonedDateTime.now());
        testMessage.setResourceStatus(ResourceStatusEnum.ACTIVE);

        testCharacter = new Character();
        testCharacter.setId(testCharacterId);
        testCharacter.setName("Test Character");
        testCharacter.setSystemPrompt("You are a helpful assistant");
        testCharacter.setCustomFields(new HashMap<>());

        // Create MessageService instance and inject mocks
        messageService = new MessageService(restTemplate, messageRepository);

        // Set up API configuration via reflection
        ReflectionTestUtils.setField(messageService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(messageService, "openRouterApiUrl", "https://api.openrouter.ai/api/v1/chat/completions");
        ReflectionTestUtils.setField(messageService, "openRouterModel", "openai/gpt-3.5-turbo");
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(messageRepository.findById(testMessageId)).thenReturn(Optional.of(testMessage));

        // Act
        Optional<Message> result = messageService.findById(testMessageId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMessageId, result.get().getId());
        verify(messageRepository).findById(testMessageId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        UUID randomId = UUID.randomUUID();
        when(messageRepository.findById(randomId)).thenReturn(Optional.empty());

        // Act
        Optional<Message> result = messageService.findById(randomId);

        // Assert
        assertFalse(result.isPresent());
        verify(messageRepository).findById(randomId);
    }

    @Test
    void testUpdateMessage_Success() {
        // Arrange
        Message updatedMessage = new Message();
        updatedMessage.setContent("Updated content");

        when(messageRepository.findById(testMessageId)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Message result = messageService.updateMessage(testMessageId, updatedMessage);

        // Assert
        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        verify(messageRepository).findById(testMessageId);
        verify(messageRepository).save(testMessage);
    }

    @Test
    void testUpdateMessage_NotFound_ThrowsException() {
        // Arrange
        UUID randomId = UUID.randomUUID();
        Message updatedMessage = new Message();
        updatedMessage.setContent("Updated content");

        when(messageRepository.findById(randomId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> messageService.updateMessage(randomId, updatedMessage));

        verify(messageRepository).findById(randomId);
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testGetAIResponse_Success() {
        // Arrange
        List<Message> history = new ArrayList<>();
        history.add(new Message(MessageRoleEnum.USER, "Hello", ZonedDateTime.now()));

        String userMessage = "How are you?";

        OpenRouterChatResponse.Choice choice = new OpenRouterChatResponse.Choice();
        OpenAIMessage aiMessage = new OpenAIMessage("assistant", "I'm doing well, thank you!");
        choice.setMessage(aiMessage);

        OpenRouterChatResponse response = new OpenRouterChatResponse();
        response.setChoices(List.of(choice));

        ResponseEntity<OpenRouterChatResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(OpenRouterChatResponse.class))).thenReturn(responseEntity);

        // Act
        Message result = messageService.getAIResponse(testCharacter, history, userMessage);

        // Assert
        assertNotNull(result);
        assertEquals(MessageRoleEnum.ASSISTANT, result.getRole());
        assertEquals("I'm doing well, thank you!", result.getContent());
        verify(restTemplate).postForEntity(anyString(), any(), eq(OpenRouterChatResponse.class));
    }

    @Test
    void testGetAIResponse_MissingApiKey_ReturnsError() {
        // Arrange
        ReflectionTestUtils.setField(messageService, "apiKey", null);
        List<Message> history = new ArrayList<>();
        String userMessage = "Test";

        // Act
        Message result = messageService.getAIResponse(testCharacter, history, userMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().contains("Error"));
        assertTrue(result.getContent().contains("API Key is missing"));
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    void testGetAIResponse_BlankApiKey_ReturnsError() {
        // Arrange
        ReflectionTestUtils.setField(messageService, "apiKey", "");
        List<Message> history = new ArrayList<>();
        String userMessage = "Test";

        // Act
        Message result = messageService.getAIResponse(testCharacter, history, userMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().contains("Error"));
        assertTrue(result.getContent().contains("API Key is missing"));
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    void testGetAIResponse_UnauthorizedError_ReturnsError() {
        // Arrange
        List<Message> history = new ArrayList<>();
        String userMessage = "Test";

        when(restTemplate.postForEntity(anyString(), any(), eq(OpenRouterChatResponse.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // Act
        Message result = messageService.getAIResponse(testCharacter, history, userMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().contains("Error"));
        assertTrue(result.getContent().contains("API Key is invalid"));
    }

    @Test
    void testGetAIResponse_TooManyRequests_ReturnsError() {
        // Arrange
        List<Message> history = new ArrayList<>();
        String userMessage = "Test";

        when(restTemplate.postForEntity(anyString(), any(), eq(OpenRouterChatResponse.class))).thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // Act
        Message result = messageService.getAIResponse(testCharacter, history, userMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().contains("Error"));
        assertTrue(result.getContent().contains("Rate limit exceeded"));
    }

    @Test
    void testGetAIResponse_RestClientException_ReturnsError() {
        // Arrange
        List<Message> history = new ArrayList<>();
        String userMessage = "Test";

        when(restTemplate.postForEntity(anyString(), any(), eq(OpenRouterChatResponse.class))).thenThrow(new RestClientException("Connection failed"));

        // Act
        Message result = messageService.getAIResponse(testCharacter, history, userMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().contains("Error"));
        assertTrue(result.getContent().contains("Could not connect to AI Service"));
    }

    @Test
    void testGetAIResponse_EmptyResponse_ReturnsError() {
        // Arrange
        List<Message> history = new ArrayList<>();
        String userMessage = "Test";

        ResponseEntity<OpenRouterChatResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(OpenRouterChatResponse.class))).thenReturn(responseEntity);

        // Act
        Message result = messageService.getAIResponse(testCharacter, history, userMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().contains("Error"));
        assertTrue(result.getContent().contains("Invalid response"));
    }

    @Test
    void testGetAIResponse_WithCustomFields() {
        // Arrange
        testCharacter.getCustomFields().put("personality", "friendly");
        testCharacter.getCustomFields().put("age", "25");

        List<Message> history = new ArrayList<>();
        String userMessage = "Hello";

        OpenRouterChatResponse.Choice choice = new OpenRouterChatResponse.Choice();
        OpenAIMessage aiMessage = new OpenAIMessage("assistant", "Hi!");
        choice.setMessage(aiMessage);

        OpenRouterChatResponse response = new OpenRouterChatResponse();
        response.setChoices(List.of(choice));

        ResponseEntity<OpenRouterChatResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(OpenRouterChatResponse.class))).thenReturn(responseEntity);

        // Act
        Message result = messageService.getAIResponse(testCharacter, history, userMessage);

        // Assert
        assertNotNull(result);
        assertEquals("Hi!", result.getContent());
    }

    @Test
    void testSoftDeleteMessagesByCharacterId_Success() {
        // Arrange
        int deletedCount = 10;
        when(messageRepository.bulkUpdateResourceStatusByCharacterId(testCharacterId, ResourceStatusEnum.DELETED)).thenReturn(deletedCount);

        // Act
        int result = messageService.softDeleteMessagesByCharacterId(testCharacterId);

        // Assert
        assertEquals(deletedCount, result);
        verify(messageRepository).bulkUpdateResourceStatusByCharacterId(testCharacterId, ResourceStatusEnum.DELETED);
    }

    @Test
    void testSoftDeleteMessagesByChatId_Success() {
        // Arrange
        Message msg1 = new Message();
        msg1.setId(UUID.randomUUID());
        msg1.setResourceStatus(ResourceStatusEnum.ACTIVE);

        Message msg2 = new Message();
        msg2.setId(UUID.randomUUID());
        msg2.setResourceStatus(ResourceStatusEnum.ACTIVE);

        List<Message> messages = Arrays.asList(msg1, msg2);

        when(messageRepository.findByChatId(testChatId)).thenReturn(messages);
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        messageService.softDeleteMessagesByChatId(testChatId);

        // Assert
        assertEquals(ResourceStatusEnum.DELETED, msg1.getResourceStatus());
        assertEquals(ResourceStatusEnum.DELETED, msg2.getResourceStatus());
        verify(messageRepository).findByChatId(testChatId);
        verify(messageRepository, times(2)).save(any(Message.class));
    }

    @Test
    void testSoftDeleteMessagesByChatId_EmptyList() {
        // Arrange
        when(messageRepository.findByChatId(testChatId)).thenReturn(new ArrayList<>());

        // Act
        messageService.softDeleteMessagesByChatId(testChatId);

        // Assert
        verify(messageRepository).findByChatId(testChatId);
        verify(messageRepository, never()).save(any(Message.class));
    }
}
