package dev.andrei.app_backend.controller;

import dev.andrei.app_backend.repository.UserRepository;
import dev.andrei.app_backend.service.AuthService;
import dev.andrei.app_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // JwtAuthenticationFilter is a @Component, so @WebMvcTest still wires it up.
    // We mock its constructor deps so the context can start; addFilters=false stops
    // the filter chain from running on each request.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    // AppBackendApplication declares a CommandLineRunner that needs JdbcTemplate;
    // JDBC autoconfig isn't loaded in the @WebMvcTest slice, so mock it too.
    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void register_WhenEmailAlreadyExists_Returns409Conflict() throws Exception {
        // --- ARRANGE ---
        // We program the service to throw the exception when ANY email is passed
        Mockito.when(authService.register(anyString(), anyString(), anyString()))
                .thenThrow(new AuthService.EmailAlreadyExistsException());

        // --- ACT & ASSERT ---
        // We combine Act & Assert using MockMvc's fluent API
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password123\", \"displayName\":\"Tester\"}"))

                // ASSERT: Check the HTTP Status is 409 Conflict
                .andExpect(status().isConflict())

                // ASSERT: Inspect the JSON body using JSONPath
                .andExpect(jsonPath("$.error").value("EMAIL_TAKEN"));
    }
}