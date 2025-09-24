package jr.chatbot.config;

import jr.chatbot.entity.User;
import jr.chatbot.entity.Character;
import jr.chatbot.enums.UserRoleEnum;
import jr.chatbot.repository.UserRepository;
import jr.chatbot.repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${app.admin.username}")
    private String username;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Value("${app.test.character.name}")
    private String testCharacterName;

    @Value("${app.test.character.description}")
    private String testCharacterDescription;

    @Value("${app.test.character.system-prompt}")
    private String testCharacterSystemPrompt;

    @Value("${app.test.character.short-greeting}")
    private String testCharacterShortGreeting;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CharacterRepository characterRepository;

    @Bean
    public ApplicationRunner initAdminUser() {
        return args -> {
            if (userRepository.findByUsername(username).isEmpty()) {
                User admin = new User();
                admin.setUsername(username);
                admin.setEmail(email);
                admin.setPasswordHash(passwordEncoder.encode(password));
                admin.setRole(UserRoleEnum.ADMIN);
                userRepository.save(admin);
                System.out.println("Admin user created.");
            } else {
                System.out.println("Admin user already exists.");
            }
        };
    }

    @Bean
    public ApplicationRunner initTestCharacter() {
        return args -> {
            if (characterRepository.findByName(testCharacterName).isEmpty()) {
                Character testChar = new Character();
                testChar.setName(testCharacterName);
                testChar.setDescription(testCharacterDescription);
                testChar.setSystemPrompt(testCharacterSystemPrompt);
                testChar.setShortGreeting(testCharacterShortGreeting);
                characterRepository.save(testChar);
                System.out.println("Test character created.");
            } else {
                System.out.println("Test character already exists.");
            }
        };
    }
}
