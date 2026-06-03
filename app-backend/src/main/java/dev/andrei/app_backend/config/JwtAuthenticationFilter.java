package dev.andrei.app_backend.config;

import dev.andrei.app_backend.model.User;
import dev.andrei.app_backend.repository.UserRepository;
import dev.andrei.app_backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        try {
            UUID userId = jwtService.parseUserId(token);
            Optional<User> user = userRepository.findById(userId);

            if (user.isEmpty()) {
                // Well-formed, correctly-signed token but no such user (e.g. the DB was reset, or the
                // token was issued by a different deployment/secret). Request stays anonymous -> 401.
                log.warn("Valid JWT for unknown user id {} on {} {} -> 401",
                        userId, request.getMethod(), request.getRequestURI());
            } else if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user.get(), null, List.of());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // Token invalid, expired, or signed with a different secret -> stay anonymous (downstream 401).
            log.warn("Rejected JWT on {} {}: {}",
                    request.getMethod(), request.getRequestURI(), e.toString());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
