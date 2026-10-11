package com.faraz.expensemanagement.security;

import com.faraz.expensemanagement.repo.UserRepo;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
class Filter extends OncePerRequestFilter {
    final Jwt jwt;
    final UserRepo repo;

    Filter(Jwt j, UserRepo r) {
        jwt = j;
        repo = r;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest q, HttpServletResponse r, FilterChain c)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(q.getMethod())) {
            c.doFilter(q, r);
            return;
        }
        String h = q.getHeader("Authorization");
        try {
            if (h != null && h.startsWith("Bearer ")) {
                String e = jwt.email(h.substring(7));
                repo.findByEmailIgnoreCase(e).ifPresent(u ->
                    SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(e, null, List.of())
                    )
                );
            }
        } catch (Exception ignored) {}
        c.doFilter(q, r);
    }
}

@Configuration
class Config {
    @Bean
    BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain chain(HttpSecurity h, Filter f) throws Exception {
        return h.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated()
                )
                .addFilterBefore(f, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}