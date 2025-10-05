package jr.chatbot.util;

import jr.chatbot.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtil {

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    public static UUID getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    public static boolean isOwner(UUID ownerId) {
        UUID currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(ownerId);
    }
}

