package com.example.shopapp.services.Token;

import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;

public interface ITokenService {
    Token addTokenEndRefreshToken(User user, String token, boolean isMobile);
    Token verifyRefreshToken(String refreshToken) throws DataNotFoundException;
}