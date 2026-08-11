package com.example.taskflow.security;

import com.example.taskflow.repository.TokenBlocklistRepository;
import com.example.taskflow.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlocklistRepository blocklist;
    private final UserSessionRepository sessions;

    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService,
            CustomUserDetailsService userDetailsService,
            TokenBlocklistRepository blocklist,
            UserSessionRepository sessions
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
        this.blocklist = blocklist;
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String jti = jwtTokenService.getJti(token);
                if (jti != null && blocklist.existsById(jti)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                String email = jwtTokenService.getSubject(token);
                var userDetails = userDetailsService.loadUserByUsername(email);
                var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                if (jti != null && !jti.isBlank()) {
                    sessions.updateLastActiveByJti(jti, Instant.now());
                }
            } catch (Exception e) {
                // Token issues -> let Security handle as unauthorized
            }
        }
        chain.doFilter(request, response);
    }
}
