package com.studygrind.config;

import com.studygrind.security.CustomUserDetailsService;
import com.studygrind.security.JwtAuthenticationFilter;
import com.studygrind.security.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain h2ConsoleFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(AntPathRequestMatcher.antMatcher("/h2-console/**"))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // ==================== PUBLIC PAGES ====================
                        // Landing and marketing pages
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/index")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/index.html")).permitAll()

                        // Legal pages
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/terms")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/terms.html")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/privacy")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/privacy.html")).permitAll()

                        // Auth pages
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/login")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/login.html")).permitAll()

                        // Error pages
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/error")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/error.html")).permitAll()

                        // Payment pages (public entry points)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/payment")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/payment/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/pay-all")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/pay-all.html")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/payment/success")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/payment/cancel")).permitAll()

                        // ==================== PUBLIC API ENDPOINTS ====================
                        // Authentication endpoints (public)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll()

                        // Course listing endpoints (public read-only)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/courses")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/courses/*/details")).permitAll()

                        // PayFast webhook (must be public for payment gateway)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/payfast/notify")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/billing/webhook/payfast")).permitAll()

                        // Health check endpoints
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/health")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/health")).permitAll()

                        // ==================== STATIC RESOURCES ====================
                        // Static resources (public)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/uploads/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/images/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/css/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/js/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/static/**")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/favicon.ico")).permitAll()

                        // ==================== DASHBOARD PAGES ====================
                        // Dashboard pages - permit all (they check auth via JavaScript)
                        // This allows redirects to work properly
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/student/dashboard")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/teacher/dashboard")).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/admin/dashboard")).permitAll()

                        // ==================== SECURED ENDPOINTS ====================
                        // Swagger/OpenAPI - Admin only
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**")).hasRole("ADMIN")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui.html")).hasRole("ADMIN")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api-docs/**")).hasRole("ADMIN")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/v3/api-docs/**")).hasRole("ADMIN")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-resources/**")).hasRole("ADMIN")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/webjars/**")).hasRole("ADMIN")

                        // Actuator endpoints - Admin only (monitoring)
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/**")).hasRole("ADMIN")
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator")).hasRole("ADMIN")

                        // Verification endpoints - require authentication
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/verification/**")).authenticated()

                        // All other API endpoints require authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow multiple origins (development and production)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://127.0.0.1:8080",
                "https://studygrind.com",
                "https://www.studygrind.com"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-CSRF-TOKEN",
                "Accept",
                "X-Requested-With",
                "X-Forwarded-For"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}