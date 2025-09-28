package jr.chatbot.service;

import jr.chatbot.dto.openrouter.OpenAIMessage;
import jr.chatbot.dto.openrouter.OpenRouterChatRequest;
import jr.chatbot.dto.openrouter.OpenRouterChatResponse;
import jr.chatbot.entity.Character;
import jr.chatbot.entity.Message;
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
public class MessageService {
    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String openRouterApiUrl;

    @Value("${openrouter.model}")
    private String openRouterModel;

    private static final String HEADER_HTTP_REFERER = "HTTP-Referer";
    private static final String HEADER_X_TITLE = "X-Title";

    private final RestTemplate restTemplate;

    public MessageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Message getAIResponse(Character character, List<Message> history, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return error("AI API Key is missing. Set OPENROUTER_API_KEY environment variable.");
        }

        try {
            var requestEntity = buildRequest(character, history, userMessage);
            var response = restTemplate.postForEntity(openRouterApiUrl, requestEntity, OpenRouterChatResponse.class);
            return parseResponse(response);
        } catch (HttpClientErrorException e) {
            return mapClientError(e);
        } catch (RestClientException e) {
            return error("Could not connect to AI Service - " + e.getMessage());
        } catch (Exception e) {
            return error("Unexpected issue processing AI response");
        }
    }

    // Helpers
    private HttpEntity<OpenRouterChatRequest> buildRequest(Character character, List<Message> history, String userMessage) {
        var payload = new OpenRouterChatRequest();
        payload.setModel(openRouterModel);
        payload.setMessages(buildMessages(character, history, userMessage));
        // Leave optional fields null to use provider defaults
        // payload.setMaxTokens(8192);
        // payload.setTemperature(1.0);

        return new HttpEntity<>(payload, buildHeaders());
    }

    private List<OpenAIMessage> buildMessages(Character character, List<Message> history, String userMessage) {
        var messages = new ArrayList<OpenAIMessage>();
        var systemPrompt = character != null ? character.getSystemPrompt() : null;
        messages.add(new OpenAIMessage("system", systemPrompt != null ? systemPrompt : ""));

        if (history != null && !history.isEmpty()) {
            for (var msg : history) {
                var role = msg.getRole() != null ? msg.getRole().name().toLowerCase() : "user";
                messages.add(new OpenAIMessage(role, msg.getContent()));
            }
        }

        messages.add(new OpenAIMessage("user", userMessage));
        return messages;
    }

    private HttpHeaders buildHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.add(HEADER_HTTP_REFERER, "http://localhost");
        headers.add(HEADER_X_TITLE, "Chatbot");
        return headers;
    }

    private Message parseResponse(ResponseEntity<OpenRouterChatResponse> response) {
        if (response == null) {
            return error("Invalid response from AI Service - Response was null");
        }

        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            return error("Invalid response from AI Service - Status: " + response.getStatusCode());
        }

        var body = response.getBody();
        if (body == null || body.getChoices() == null || body.getChoices().isEmpty()) {
            return error("Invalid response from AI Service - Empty body/choices");
        }

        var aiMessage = body.getChoices().getFirst().getMessage();
        if (aiMessage == null || aiMessage.getContent() == null) {
            return error("Received empty content from AI");
        }

        return assistant(aiMessage.getContent());
    }

    private Message mapClientError(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return error("AI API Key is invalid or missing.");
        }
        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            return error("Rate limit exceeded for AI API.");
        }
        return error("Failed to communicate with AI. Status: " + e.getStatusCode());
    }

    private Message assistant(String content) {
        return new Message(MessageRoleEnum.ASSISTANT, content, ZonedDateTime.now());
    }

    private Message error(String text) {
        return assistant("[Error: " + text + "]");
    }
}
