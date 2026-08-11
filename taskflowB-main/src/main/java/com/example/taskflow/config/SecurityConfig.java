package com.example.taskflow.config;

import com.example.taskflow.security.CustomUserDetailsService;
import com.example.taskflow.security.JwtAuthenticationFilter;
import com.example.taskflow.security.JwtTokenService;
import com.example.taskflow.repository.TokenBlocklistRepository;
import com.example.taskflow.repository.UserSessionRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// CORS imports
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt hashing per SRS NFR-04
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtTokenService jwtTokenService,
                                           CustomUserDetailsService userDetailsService,
                                           TokenBlocklistRepository tokenBlocklistRepository,
                                           UserSessionRepository userSessionRepository) throws Exception {
        http
          .csrf(csrf -> csrf.disable())
          // ✅ Enable CORS so Spring Security applies the CorsConfigurationSource below
          .cors(cors -> {})
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(reg -> reg
              .requestMatchers("/api/auth/**").permitAll()
              .anyRequest().authenticated())
          .addFilterBefore(new JwtAuthenticationFilter(jwtTokenService, userDetailsService, tokenBlocklistRepository, userSessionRepository),
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ Global CORS configuration for dev: Angular at http://localhost:4200
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // Allow your Angular dev origin (add more origins here as needed)
        cfg.setAllowedOrigins(java.util.List.of("http://localhost:4200"));
        // Allow the methods your app uses (preflight will check these)
        cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        // Allow headers sent by the Angular app (Authorization for JWT, Content-Type for JSON)
        cfg.setAllowedHeaders(java.util.List.of("Authorization","Content-Type"));
        // For JWT in localStorage you usually do NOT need credentials (cookies)
        // cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
