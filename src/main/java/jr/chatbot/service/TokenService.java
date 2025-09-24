package jr.chatbot.service;

import jr.chatbot.entity.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private final Map<String, UUID> tokenStore = new ConcurrentHashMap<>();

    public String issueToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user.getId());
        return token;
        }

    public Optional<UUID> getUserIdForToken(String token) {
        return Optional.ofNullable(tokenStore.get(token));
    }

    public void revokeToken(String token) {
        if (token != null) {
            tokenStore.remove(token);
        }
    }

    public boolean isValid(String token) {
        return token != null && tokenStore.containsKey(token);
    }
}

