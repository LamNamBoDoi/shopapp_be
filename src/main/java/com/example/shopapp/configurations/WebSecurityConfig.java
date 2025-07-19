package com.example.shopapp.configurations;

import com.example.shopapp.filters.JwtTokenFilter;
import com.example.shopapp.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static com.example.shopapp.models.Role.ADMIN;
import static org.springframework.http.HttpMethod.*;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // Trả về 401 thay vì redirect
                )
                .authorizeHttpRequests(requests -> {
                    requests.requestMatchers(HttpMethod.GET,
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/swagger-resources/**",
                                    "/webjars/**",
                                    "/swagger-ui.html",
                                    String.format("%s/comments", apiPrefix),
                                    String.format("%s/comments/**", apiPrefix))
                            .permitAll()
                            .requestMatchers(HttpMethod.POST,
                                    String.format("%s/users/register", apiPrefix),
                                    String.format("%s/users/login", apiPrefix),
                                    String.format("%s/users/refresh-token", apiPrefix)
                            ).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/categories", apiPrefix)).permitAll()
                            .requestMatchers(POST,
                                    String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(PUT,
                                    String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(DELETE,
                                    String.format("%s/categories/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(GET,
                                    String.format("%s/images/**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/products/images/**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/products", apiPrefix),
                                    String.format("%s/products/**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/products/details", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/products/by-ids", apiPrefix),
                                    String.format("%s/products/by-ids/**", apiPrefix)).permitAll()
                            .requestMatchers(POST,
                                    String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(PUT,
                                    String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(DELETE,
                                    String.format("%s/products/**", apiPrefix)).hasRole(Role.ADMIN)

                            .requestMatchers(POST,
                                    String.format("%s/orders/**", apiPrefix)).hasRole(Role.USER)
                            .requestMatchers(GET,
                                    String.format("%s/orders/**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/orders/get-orders-by-keyword", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(GET,
                                    String.format("%s/orders/user/**", apiPrefix)).permitAll()
                            .requestMatchers(PUT,
                                    String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(DELETE,
                                    String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)

                            .requestMatchers(POST,
                                    String.format("%s/wishlists", apiPrefix)).hasRole(Role.USER) // Chỉ USER mới được tạo wishlist
                            .requestMatchers(GET,
                                    String.format("%s/wishlists", apiPrefix)).permitAll() // Nếu có GET /wishlists
                            .requestMatchers(GET,
                                    String.format("%s/wishlists/user/**", apiPrefix)).permitAll() // Lấy wishlist theo user (ai cũng xem được)
                            .requestMatchers(GET,
                                    String.format("%s/wishlists/product/**", apiPrefix)).permitAll() // Lấy wishlist theo product
                            .requestMatchers(GET,
                                    String.format("%s/wishlists/**", apiPrefix)).permitAll() // GET chi tiết wishlist theo id
                            .requestMatchers(DELETE,
                                    String.format("%s/wishlists/**", apiPrefix)).hasRole(Role.USER) // USER được xóa wishlist (có thể thay bằng ADMIN nếu cần kiểm soát chặt)

                            .requestMatchers(POST,
                                    String.format("%s/reviews", apiPrefix)).hasRole(Role.USER) // Chỉ USER mới được tạo reviews
                            .requestMatchers(GET,
                                    String.format("%s/reviews", apiPrefix)).permitAll() // Nếu có GET /reviews
                            .requestMatchers(GET,
                                    String.format("%s/reviews/user/**", apiPrefix)).permitAll() // Lấy reviews theo user (ai cũng xem được)
                            .requestMatchers(GET,
                                    String.format("%s/reviews/product/**", apiPrefix)).permitAll() // Lấy reviews theo product
                            .requestMatchers(GET,
                                    String.format("%s/reviews/**", apiPrefix)).permitAll() // GET chi tiết reviews theo id
                            .requestMatchers(DELETE,
                                    String.format("%s/reviews/**", apiPrefix)).hasRole(Role.USER) // USER được xóa reviews (có thể thay bằng ADMIN nếu cần kiểm soát chặt)


                            .requestMatchers(POST,
                                    String.format("%s/order_details/**", apiPrefix)).hasRole(Role.USER)
                            .requestMatchers(GET,
                                    String.format("%s/order_details/**", apiPrefix)).hasAnyRole(Role.ADMIN, Role.USER)
                            .requestMatchers(PUT,
                                    String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)
                            .requestMatchers(DELETE,
                                    String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)

                            .anyRequest().authenticated();
                })
                .csrf(AbstractHttpConfigurer::disable);
        http.cors(
                new Customizer<CorsConfigurer<HttpSecurity>>() {
                    @Override
                    public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.setAllowedOrigins(List.of("*"));
                        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
                        configuration.setExposedHeaders(List.of("x-auth-token"));
                        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                        source.registerCorsConfiguration("/**", configuration);
                        httpSecurityCorsConfigurer.configurationSource(source);
                    }
                }
        );
        return http
                .build();
    }
}
