package jr.chatbot.service;

import jr.chatbot.dto.openrouter.OpenAIMessage;
import jr.chatbot.dto.openrouter.OpenRouterChatRequest;
import jr.chatbot.dto.openrouter.OpenRouterChatResponse;
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
    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String openRouterApiUrl;

    @Value("${openrouter.model}")
    private String openRouterModel;

    private final RestTemplate restTemplate;

    public ChatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ChatMessage getAIResponse(Character character, List<ChatMessage> history, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return new ChatMessage(MessageRoleEnum.ASSISTANT, "[Error: AI API Key is missing. Set OPENROUTER_API_KEY environment variable.]" , ZonedDateTime.now());
        }

        var messagesToSend = new ArrayList<OpenAIMessage>();
        messagesToSend.add(new OpenAIMessage("system", character.getSystemPrompt()));
        if (history != null) {
            messagesToSend.addAll(history.stream()
                    .map(msg -> new OpenAIMessage(
                            msg.getRole() != null ? msg.getRole().name().toLowerCase() : "user",
                            msg.getContent()
                    ))
                    .toList());
        }
        messagesToSend.add(new OpenAIMessage("user", userMessage));

        var requestPayload = new OpenRouterChatRequest();
        requestPayload.setModel(openRouterModel);
        requestPayload.setMessages(messagesToSend);
        // Optionally tune these; keep null to use provider defaults
        // requestPayload.setMaxTokens(8192);
        // requestPayload.setTemperature(1.0);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        // Optional but recommended by OpenRouter
        headers.add("HTTP-Referer", "http://localhost");
        headers.add("X-Title", "Chatbot");

        var entity = new HttpEntity<>(requestPayload, headers);

        try {
            var response = restTemplate.postForEntity(
                    openRouterApiUrl,
                    entity,
                    OpenRouterChatResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null &&
                    response.getBody().getChoices() != null && !response.getBody().getChoices().isEmpty()) {

                var aiMessage = response.getBody().getChoices().getFirst().getMessage();
                if (aiMessage != null && aiMessage.getContent() != null) {
                    return new ChatMessage(MessageRoleEnum.ASSISTANT, aiMessage.getContent(), ZonedDateTime.now());
                } else {
                    return new ChatMessage(MessageRoleEnum.ASSISTANT, "[Error: Received empty content from AI]", ZonedDateTime.now());
                }
            } else {
                return new ChatMessage(MessageRoleEnum.ASSISTANT, "[Error: Invalid response from AI Service - Status: " + response.getStatusCode() + "]", ZonedDateTime.now());
            }

        } catch (HttpClientErrorException e) {
            var errorMsg = "[Error: Failed to communicate with AI. Status: " + e.getStatusCode() + "]";
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

