package jr.chatbot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jr.chatbot.dto.LoginRequest;
import jr.chatbot.dto.LoginResponse;
import jr.chatbot.dto.RegisterRequest;
import jr.chatbot.dto.RegisterResponse;
import jr.chatbot.entity.User;
import jr.chatbot.service.JwtService;
import jr.chatbot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:5173", "http://127.0.0.1:4200"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if ((request.getUsername() == null && request.getEmail() == null) || request.getPassword() == null) {
            var error = new HashMap<>();
            error.put("message", "username/email and password are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        var userOpt = (request.getUsername() != null && !request.getUsername().isBlank()) ? userService.findByUsername(request.getUsername()) : userService.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                String token = jwtService.generateToken(user);
                LoginResponse response = new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
                return ResponseEntity.ok(response);
            }
        }
        var error = new HashMap<>();
        error.put("message", "Invalid username/email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, @RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(Map.of("status", "logged out"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            var error = new HashMap<>();
            error.put("message", "Username is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            var error = new HashMap<>();
            error.put("message", "Email is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            var error = new HashMap<>();
            error.put("message", "Password is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        try {
            User user = userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
            RegisterResponse response = new RegisterResponse(user.getId(), user.getUsername(), user.getEmail(), "User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            var error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }
}
