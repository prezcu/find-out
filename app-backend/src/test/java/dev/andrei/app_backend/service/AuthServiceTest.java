package dev.andrei.app_backend.service;

import dev.andrei.app_backend.dto.auth.AuthResponse;
import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


// 1. Tell JUnit to use Mockito to handle our fake objects
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WhenNewUserIsValid_ReturnTokenAndUpdateDb() {
        // arrange
        String email = "USER@TEST.COM";
        String rawPassword = "parola1234";
        JwtService.IssuedToken token = new JwtService.IssuedToken("fake_token", 1234);

        Mockito.when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("parola1234")).thenReturn("fake_hash");
        Mockito.when(jwtService.issueFor(any(UUID.class))).thenReturn(token);
        // act
        AuthResponse response = authService.register(email, rawPassword, "  Test User  ");

        // assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        // new registered user verification
        assertEquals("user@test.com", savedUser.getEmail());
        assertEquals("fake_hash", savedUser.getPasswordHash());
        assertEquals("Test User", savedUser.getDisplayName()); // trimmed
        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        // token verification
        assertEquals("fake_token", response.token());
        assertEquals(1234, response.expiresAt());

    }

    @Test
    void register_WhenUserAlreadyExists_ThrowsException(){
        String email = "user@test.com";
        String rawPassword = "parola12345";

        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);

        AuthService.EmailAlreadyExistsException exception = assertThrows(
                AuthService.EmailAlreadyExistsException.class,
                () -> authService.register(email, rawPassword, "Test User")
        );
    }

}
