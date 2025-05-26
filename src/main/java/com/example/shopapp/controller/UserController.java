package com.example.shopapp.controller;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.dtos.UserLoginDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.User;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.LoginResponse;
import com.example.shopapp.response.RegisterResponse;
import com.example.shopapp.response.UserResponse;
import com.example.shopapp.services.User.IUserService;
import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController extends TranslateMessages {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;
    @PostMapping("/register")
    // can we register an "admin" user?
    public ResponseEntity<RegisterResponse> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ){
        try{
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(
                        RegisterResponse.builder()
                                .message(errorMessages.toString())
                                .build()
                );
            }
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
                return ResponseEntity.badRequest().body(RegisterResponse.builder()
                                .message(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH))
                        .build());
            }
            User user = userService.createUser(userDTO);
            return ResponseEntity.ok().body(RegisterResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESS))
                            .user(user)
                    .build());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(
                    RegisterResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.ERROR_MESSAGE))
                            .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO
            ){
        try {
            String token = userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());
            return ResponseEntity.ok(LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESS))
                            .token(token)
                    .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                    .build());
        }
    }

    // Lấy ra thông tin chi tiết của người dùng thông qua token truyền vào
    @PostMapping("/details")
    public ResponseEntity<ApiResponse<?>> getUserDetails(
            @RequestHeader("Authorization") String token
    ) {
        try {
            String extractedToken = token.substring(7);
            User user = userService.getUserDetailsFromToken(extractedToken);
            return ResponseEntity.ok(ApiResponse.<UserResponse>builder().success(true)
                    .payload(UserResponse.fromUser(user)).build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<UserResponse>builder()
                            .message(translate(MessageKeys.MESSAGE_ERROR_GET)).error(e.getMessage()).build()
            );
        }
    }
}
