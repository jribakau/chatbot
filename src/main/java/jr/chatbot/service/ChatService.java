package jr.chatbot.service;

import jr.chatbot.dto.deepseek.DeepSeekChatRequest;
import jr.chatbot.dto.deepseek.DeepSeekChatResponse;
import jr.chatbot.entity.Character;
import jr.chatbot.entity.ChatMessage;
import jr.chatbot.enums.MessageRoleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String deepseekApiUrl;

    @Value("${deepseek.model}")
    private String deepseekModel;

    private final RestTemplate restTemplate;

    public ChatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ChatMessage getAIResponse(Character character, List<ChatMessage> history, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return new ChatMessage(MessageRoleEnum.ASSISTANT, "[Error: AI API Key is missing. Set DEEPSEEK_API_KEY environment variable.]" , ZonedDateTime.now());
        }

        List<ChatMessage> messagesToSend = new ArrayList<>();
        messagesToSend.add(new ChatMessage(MessageRoleEnum.SYSTEM, character.getSystemPrompt(), ZonedDateTime.now()));
        messagesToSend.addAll(history);
        messagesToSend.add(new ChatMessage(MessageRoleEnum.USER, userMessage, ZonedDateTime.now()));

        var requestPayload = new DeepSeekChatRequest();
        requestPayload.setModel(deepseekModel);
        requestPayload.setMessages(messagesToSend);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<DeepSeekChatRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<DeepSeekChatResponse> response = restTemplate.postForEntity(
                    deepseekApiUrl,
                    entity,
                    DeepSeekChatResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null &&
                    response.getBody().getChoices() != null && !response.getBody().getChoices().isEmpty()) {

                ChatMessage aiMessage = response.getBody().getChoices().getFirst().getMessage();
                if (aiMessage != null && aiMessage.getContent() != null) {
                    aiMessage.setRole(MessageRoleEnum.ASSISTANT);
                    return aiMessage;
                } else {
                    return new ChatMessage(MessageRoleEnum.ASSISTANT, "[Error: Received empty content from AI]", ZonedDateTime.now());
                }
            } else {
                return new ChatMessage(MessageRoleEnum.ASSISTANT, "[Error: Invalid response from AI Service - Status: " + response.getStatusCode() + "]", ZonedDateTime.now());
            }

        } catch (HttpClientErrorException e) {
            String errorMsg = "[Error: Failed to communicate with AI. Status: " + e.getStatusCode() + "]";
            if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                errorMsg = "[Error: AI API Key is invalid or missing.]";
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                errorMsg = "[Error: Rate limit exceeded for AI API.]";
            }
            return new ChatMessage(MessageRoleEnum.ASSISTANT, errorMsg, ZonedDateTime.now());
        } catch (RestClientException e) {
            return new ChatMessage(MessageRoleEnum.ASSISTANT, "[Error: Could not connect to AI Service - " + e.getMessage() + "]", ZonedDateTime.now());
        } catch (Exception e) {
            return new ChatMessage(MessageRoleEnum.ASSISTANT, "[Error: Unexpected issue processing AI response]", ZonedDateTime.now());
        }
    }
}
