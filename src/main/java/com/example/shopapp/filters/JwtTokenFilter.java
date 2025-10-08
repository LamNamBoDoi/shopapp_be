package com.example.shopapp.filters;

import com.example.shopapp.components.JwtTokenUtils;
import com.example.shopapp.models.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    @Value("${api.prefix}")
    private String apiPrefix;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws IOException, ServletException {

        if (isBypassToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
            final String token = authHeader.substring(7);
            final String phoneNumber = jwtTokenUtils.extractPhonenumber(token);
            if (phoneNumber != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails = (User) userDetailsService.loadUserByUsername(phoneNumber);
                if (jwtTokenUtils.validateToken(token, userDetails)) {
                    Claims claims = jwtTokenUtils.extractAllClaims(token);
                    List<String> roles = claims.get("roles", List.class);

                    // chuyển role thành danh sách quyền
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities
                            );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

    }

    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                // Public API
                Pair.of(String.format("%s/images/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/products**", apiPrefix), "GET"),
                Pair.of(String.format("%s/products/images/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/products/details**", apiPrefix), "GET"),
                Pair.of(String.format("%s/comments**", apiPrefix), "GET"),
                Pair.of(String.format("%s/categories**", apiPrefix), "GET"),
                Pair.of(String.format("%s/categories/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/reviews/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/refresh-token", apiPrefix), "POST"),
                Pair.of(String.format("%s/healthcheck/health", apiPrefix), "GET"),
                Pair.of(String.format("%s/actuator/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/payment-result", apiPrefix), "GET"),

                Pair.of("/ws/**", "GET"),
                Pair.of("/ws-sockjs/**", "GET"),

                // Swagger & Docs
                Pair.of("/swagger-ui/**", "GET"),
                Pair.of("/v3/api-docs/**", "GET"),
                Pair.of("/swagger-resources/**", "GET"),
                Pair.of("/webjars/**", "GET"),
                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/api-docs/**", "GET"),
                Pair.of("/error", "GET"),

                // ===== Payment callbacks (whitelist) =====
                Pair.of(String.format("%s/payments/vnpay-return", apiPrefix), "GET"),
                Pair.of(String.format("%s/payments/vnpay-notify", apiPrefix), "GET"),
                Pair.of(String.format("%s/payments/momo-return", apiPrefix), "GET"),
                Pair.of(String.format("%s/payments/momo-notify", apiPrefix), "POST"),
                Pair.of(String.format("%s/payments/zalopay/callback", apiPrefix), "POST")

                );

        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();
        AntPathMatcher matcher = new AntPathMatcher();

        for (Pair<String, String> bypassToken : bypassTokens) {
            String pathPattern = bypassToken.getFirst();
            String method = bypassToken.getSecond();

            if (matcher.match(pathPattern, requestPath) &&
                    requestMethod.equalsIgnoreCase(method)) {
                System.out.printf("Bypass: %s %s%n", requestMethod, requestPath);
                return true;
            }
        }
        return false;
    }

}
