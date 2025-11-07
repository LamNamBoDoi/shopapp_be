package com.example.shopapp.configurations;

import com.example.shopapp.filters.JwtTokenFilter;
import com.example.shopapp.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage()))
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage()))
                )
                .authorizeHttpRequests(this::configureAuthorization)
                .build();
    }

    private void configureAuthorization(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
                    <HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {

        // Public endpoints - không cần authentication
        requests.requestMatchers(
                // Swagger/OpenAPI endpoints
                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**",
                "/swagger-resources/**", "/webjars/**", "/swagger-ui.html",
                "/ws/**", "/ws-sockjs/**"
        ).permitAll();

        // Public GET endpoints
        requests.requestMatchers(HttpMethod.GET,
                String.format("%s/healthcheck/**", apiPrefix),
                String.format("%s/actuator/**", apiPrefix),
                String.format("%s/comments/**", apiPrefix),
                String.format("%s/images/**", apiPrefix),
                String.format("%s/orders_details/**", apiPrefix),
                String.format("%s/products/**", apiPrefix),
                String.format("%s/products/details**", apiPrefix),
                String.format("%s/reviews/**", apiPrefix),
                String.format("%s/categories/**", apiPrefix)
        ).permitAll();

        // Public POST endpoints (registration, login, refresh token)
        requests.requestMatchers(HttpMethod.POST,
                String.format("%s/users/register", apiPrefix),
                String.format("%s/users/login", apiPrefix),
                String.format("%s/users/refresh-token", apiPrefix)
        ).permitAll();

        // Public POST/GET endpoints cho thanh toán
        requests.requestMatchers(
                String.format("%s/payments/vnpay-return", apiPrefix),
                String.format("%s/payments/vnpay-notify", apiPrefix),
                String.format("%s/payments/momo-return", apiPrefix),
                String.format("%s/payments/momo-notify", apiPrefix),
                String.format("%s/payment-result", apiPrefix),
                String.format("%s/payments/zalopay/callback", apiPrefix)
        ).permitAll();

        // Endpoints requiring USER or ADMIN role
        requests.requestMatchers(
                String.format("%s/notifications/**", apiPrefix),
                String.format("%s/chats/**", apiPrefix),
                "/ws/**"
        ).hasAnyRole(Role.USER, Role.ADMIN);

        // All other requests require authentication
        requests.anyRequest().authenticated();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Chỉ cho phép các origins cụ thể thay vì "*"
        configuration.setAllowedOrigins(allowedOrigins);

        // Cho phép credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Allowed methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
                "authorization", "content-type", "x-auth-token",
                "x-requested-with", "accept", "origin"
        ));

        // Exposed headers
        configuration.setExposedHeaders(List.of("x-auth-token"));

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}