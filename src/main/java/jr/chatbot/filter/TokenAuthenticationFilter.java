package jr.chatbot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jr.chatbot.service.JwtService;
import jr.chatbot.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public TokenAuthenticationFilter(JwtService jwtService, @Lazy UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                if (!jwtService.validateToken(token)) {
                    sendUnauthorizedResponse(response, "Token expired or invalid");
                    return;
                }

                UUID userId = jwtService.extractUserId(token);
                userService.findById(userId).ifPresent(user -> {
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                        var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                });
            } catch (Exception e) {
                sendUnauthorizedResponse(response, "Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\",\"status\":401}");
    }
}
