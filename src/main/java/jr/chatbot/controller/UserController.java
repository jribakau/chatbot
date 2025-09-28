package jr.chatbot.controller;

import jr.chatbot.dto.LoginRequest;
import jr.chatbot.dto.LoginResponse;
import jr.chatbot.entity.User;
import jr.chatbot.service.TokenService;
import jr.chatbot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:5173", "http://127.0.0.1:4200"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if ((request.getUsername() == null && request.getEmail() == null) || request.getPassword() == null) {
            var error = new HashMap<>();
            error.put("message", "username/email and password are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        var userOpt = (request.getUsername() != null && !request.getUsername().isBlank())
                ? userService.findByUsername(request.getUsername())
                : userService.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                String token = tokenService.issueToken(user);
                LoginResponse response = new LoginResponse(
                        token,
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole().name()
                );
                return ResponseEntity.ok(response);
            }
        }
        var error = new HashMap<>();
        error.put("message", "Invalid username/email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, @RequestBody(required = false) Map<String, String> body) {
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (body != null) {
            token = body.get("token");
        }
        if (token != null) {
            tokenService.revokeToken(token);
        }
        return ResponseEntity.ok(Map.of("status", "logged out"));
    }
}

