package com.example.shopapp.services.User;

import com.example.shopapp.dtos.RefreshTokenDTO;
import com.example.shopapp.dtos.UpdateUserDTO;
import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.PermissionDenyException;
import com.example.shopapp.models.User;
import com.example.shopapp.response.LoginResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String phoneNumber, String password) throws DataNotFoundException;
    User getUserDetailsFromToken(String token) throws Exception;
    User updateUser(Long userId, UpdateUserDTO updateUserDto) throws Exception;
    LoginResponse refreshToken(RefreshTokenDTO refreshTokenDTO) throws DataNotFoundException, PermissionDenyException;

    Page<User> findAllUsers(String keyword, Pageable pageable);

    void resetPassword(Long userId, String newPassword) throws Exception;

    void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException;

    List<User> findAllUsersNoPage();
}
