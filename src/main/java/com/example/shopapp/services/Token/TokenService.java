package com.example.shopapp.services.Token;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.TokenRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenService extends TranslateMessages implements ITokenService{

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    // số thiết bị nhập
    private static final int MAX_TOKENS = 3;

    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.expiration-refresh-token}")
    private int expirationRefreshToken;

    @Override
    public Token addTokenEndRefreshToken(User user, String token, boolean isMobile) {
        List<Token> userTokens = tokenRepository.findByUser(user);
        int tokensCount = userTokens.size();

        // số lượng token vượt quá giới hạn
        if(tokensCount >= MAX_TOKENS){
            boolean hasNoMobileToken = !userTokens.stream().allMatch(Token::isMobile);
            Token tokenToDelete;
            if(hasNoMobileToken){
                tokenToDelete = userTokens.stream()
                        .filter(userToken -> !userToken.isMobile())
                        .findFirst()
                        .orElse(userTokens.getFirst());
            }else{
                // chúng ta sẽ xóa token đầu tiên trong danh sách
                tokenToDelete = userTokens.getFirst();
            }
            tokenRepository.delete(tokenToDelete);
            log.info("Deleted old token for user: {} to make room for new token", user.getId());
        }

        Instant now = Instant.now();
        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .refreshToken(UUID.randomUUID().toString())
                .tokenType("Bearer")
                .expirationTime(now.plusSeconds(expiration))
                .refreshExpirationTime(now.plusSeconds(expirationRefreshToken))
                .revoked(false)
                .expired(false)
                .isMobile(isMobile)
                .build();

        Token savedToken = tokenRepository.save(newToken);

        // Debug logging
        log.info("Created new token for user: {} - Access expires: {}, Refresh expires: {}",
                user.getId(), savedToken.getExpirationTime(), savedToken.getRefreshExpirationTime());

        return savedToken;
    }

    @Override
    public Token verifyRefreshToken(String refreshToken) throws DataNotFoundException {
        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new DataNotFoundException(translate(MessageKeys.NOT_FOUND)));

//        if(token.getRefreshExpirationTime().compareTo(Instant.now()) < 0){
//            log.warn("Refresh token expired for user: {}, deleting token", token.getUser().getId());
//            tokenRepository.delete(token);
//            throw new RuntimeException("Refresh token expired");
//        }

        log.info("Refresh token verified successfully for user: {}", token.getUser().getId());
        return token;
    }

    // Thêm method để kiểm tra access token hết hạn
    public boolean isAccessTokenExpired(Token token) {
        return token.getExpirationTime().compareTo(Instant.now()) < 0;
    }

    // Method để cleanup các token hết hạn (có thể chạy định kỳ)
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        List<Token> expiredTokens = tokenRepository.findAll().stream()
                .filter(token -> token.getRefreshExpirationTime().compareTo(now) < 0)
                .toList();

        if (!expiredTokens.isEmpty()) {
            log.info("Cleaning up {} expired refresh tokens", expiredTokens.size());
            tokenRepository.deleteAll(expiredTokens);
        }
    }

    // Method để revoke token khi logout
    public void revokeToken(String token) {
        tokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            t.setExpired(true);
            tokenRepository.save(t);
            log.info("Token revoked for user: {}", t.getUser().getId());
        });
    }

    // Method để revoke tất cả token của user (khi đổi mật khẩu, etc.)
    public void revokeAllUserTokens(User user) {
        List<Token> userTokens = tokenRepository.findByUser(user);
        userTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(userTokens);
        log.info("All tokens revoked for user: {}", user.getId());
    }
}