package com.example.shopapp.services.Token;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.TokenRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
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
        }

        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .refreshToken(UUID.randomUUID().toString())
                .tokenType("Bearer")
                .expirationTime(Instant.now().plusMillis(expiration))
                .refreshExpirationTime(Instant.now().plusMillis(expirationRefreshToken))
                .revoked(false)
                .expired(false)
                .isMobile(isMobile)
                .build();


        return  tokenRepository.save(newToken);
    }

    @Override
    public Token verifyRefreshToken(String refreshToken) throws DataNotFoundException {
        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new DataNotFoundException(translate(MessageKeys.NOT_FOUND)));

        if(token.getExpirationTime().compareTo(Instant.now())<0){
            tokenRepository.delete(token);
            throw  new RuntimeException("Refresh token expired");
        }
        return token;
    }
}
