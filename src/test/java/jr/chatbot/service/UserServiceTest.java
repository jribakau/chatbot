package jr.chatbot.service;

import jr.chatbot.entity.User;
import jr.chatbot.enums.UserRoleEnum;
import jr.chatbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword123");
        testUser.setRole(UserRoleEnum.USER);
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        String username = "newuser";
        String email = "newuser@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.registerUser(username, email, rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(encodedPassword, result.getPasswordHash());
        assertEquals(UserRoleEnum.USER, result.getRole());

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_UsernameExists_ThrowsException() {
        // Arrange
        String username = "existinguser";
        String email = "new@example.com";
        String rawPassword = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(username, email, rawPassword));

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailExists_ThrowsException() {
        // Arrange
        String username = "newuser";
        String email = "existing@example.com";
        String rawPassword = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(username, email, rawPassword));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findById(testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUserId, result.get().getId());
        verify(userRepository).findById(testUserId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        UUID randomId = UUID.randomUUID();
        when(userRepository.findById(randomId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(randomId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(randomId);
    }

    @Test
    void testFindByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testFindByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail("notfound@example.com");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("notfound@example.com");
    }

    @Test
    void testAuthenticate_Success() {
        // Arrange
        String username = "testuser";
        String rawPassword = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, testUser.getPasswordHash())).thenReturn(true);

        // Act
        boolean result = userService.authenticate(username, rawPassword);

        // Assert
        assertTrue(result);
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, testUser.getPasswordHash());
    }

    @Test
    void testAuthenticate_WrongPassword() {
        // Arrange
        String username = "testuser";
        String wrongPassword = "wrongpassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, testUser.getPasswordHash())).thenReturn(false);

        // Act
        boolean result = userService.authenticate(username, wrongPassword);

        // Assert
        assertFalse(result);
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(wrongPassword, testUser.getPasswordHash());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        // Arrange
        String username = "nonexistent";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.authenticate(username, password);

        // Assert
        assertFalse(result);
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}

