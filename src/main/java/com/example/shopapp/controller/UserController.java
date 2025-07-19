package com.example.shopapp.controller;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.RefreshTokenDTO;
import com.example.shopapp.dtos.UpdateUserDTO;
import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.dtos.UserLoginDTO;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.LoginResponse;
import com.example.shopapp.response.UserResponse;
import com.example.shopapp.response.user.UserRegisterResponse;
import com.example.shopapp.services.Token.TokenService;
import com.example.shopapp.services.User.IUserService;
import com.example.shopapp.utils.MessageKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.Media;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController extends TranslateMessages {
    private final IUserService userService;
    private final TokenService tokenService;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult bindingResult, HttpServletRequest request
    ) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errorMessages = bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .message(translate(MessageKeys.ERROR_MESSAGE))
                                .errors(errorMessages.stream().map(this::translate)
                                        .toList())
                                .build()
                );
            }
            if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .message(translate(MessageKeys.ERROR_MESSAGE))
                        .error(translate(MessageKeys.PASSWORD_NOT_MATCH)).build()
                );
            }
            User newUser = userService.createUser(userDTO);
            String tokenGenerator = userService.login(userDTO.getPhoneNumber(), userDTO.getPassword());
            String userAgent = request.getHeader("User-Agent");
            Token token = tokenService.addTokenEndRefreshToken(newUser, tokenGenerator, isMoblieDevice(userAgent));

            return ResponseEntity.ok().body(ApiResponse.builder().success(true)
                    .message(translate(MessageKeys.REGISTER_SUCCESS))
                    .payload(
                            LoginResponse.builder()
                                    .token(token.getToken())
                                    .refreshToken(token.getRefreshToken())
                                    .user(UserRegisterResponse.fromUser(newUser)).build()
                    ).build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .error(e.getMessage())
                    .message(translate(MessageKeys.ERROR_MESSAGE)).error(e.getMessage()).build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest request,
            BindingResult bindingResult
    ) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errorMessages = bindingResult.getFieldErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(
                        ApiResponse.<LoginResponse>builder()
                                .message(translate(MessageKeys.LOGIN_FAILED))
                                .errors(errorMessages.stream()
                                        .map(this::translate)
                                        .toList()).build()
                );
            }
            String tokenGenerator = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword()
            );

            // kiểm tra là điện thoại hay web đăng nhập
            String userAgent = request.getHeader("User-Agent");
            User user = userService.getUserDetailsFromToken(tokenGenerator);
            Token token = tokenService.addTokenEndRefreshToken(user, tokenGenerator, isMoblieDevice(userAgent));

            ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                    .success(true)
                    .message(translate((MessageKeys.LOGIN_SUCCESS)))
                    .payload(LoginResponse.builder()
                            .token(token.getToken())
                            .refreshToken(token.getRefreshToken())
                            .user(UserRegisterResponse.fromUser(user))
                            .build())
                    .build();
            return ResponseEntity.ok().body(apiResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<LoginResponse>builder()
                            .message(translate(MessageKeys.LOGIN_FAILED))
                            .error(e.getMessage()).build()
            );
        }
    }

    // Lấy ra thông tin chi tiết của người dùng thông qua token truyền vào
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<?>> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            if (!authorizationHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid Authorization header");
            }
            String extractedToken = authorizationHeader.substring(7);
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

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        try {
            ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                    .success(true)
                    .message(translate(MessageKeys.REFRESH_TOKEN_SUCCESS))
                    .payload(userService.refreshToken(refreshTokenDTO))
                    .build();
            return ResponseEntity.ok().body(apiResponse);
        } catch (Exception e) {
            ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                    .message(translate(MessageKeys.ERROR_REFRESH_TOKEN))
                    .error(e.getMessage()).build();
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    //kiểm tra xem thiết bị đang đăng nhập có phải mobile không
    private boolean isMoblieDevice(String userAgent) {
        return userAgent.toLowerCase().contains("mobile");
    }

    @PostMapping(value = "/details/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<?>> updateUserDetails(
            @PathVariable Long userId,
            @ModelAttribute @Valid UpdateUserDTO updateUserDTO,
            @RequestHeader("Authorization") String token
    ) {
        try {
            if (!token.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid Authorization header");
            }
            String extractedToken = token.substring(7);
            User user = userService.getUserDetailsFromToken(extractedToken);
            if (!user.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            User updateUser = userService.updateUser(userId, updateUserDTO);
            return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message(translate(MessageKeys.MESSAGE_UPDATE_GET))
                    .payload(UserResponse.fromUser(updateUser)).build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<UserResponse>builder()
                            .message(translate(MessageKeys.MESSAGE_ERROR_GET)).error(e.getMessage()).build()
            );
        }
    }

    @PutMapping("/block/{userId}/{active}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<?>> blockOrEnable(
            @Valid @PathVariable("userId") long id,
            @Valid @PathVariable("active") int active
    ) {
        try {
            userService.blockOrEnable(id, active > 0);
            if (active > 0) {
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message(translate(MessageKeys.USER_ID_UNLOCKED))
                        .build());
            }
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(translate(MessageKeys.USER_ID_LOCKED))
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .error(e.getMessage()).build());
        }
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "", name = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "limit") int limit
    ){
        try{
            PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
            Page<UserResponse> usersPage = userService.findAllUsers(keyword, pageRequest)
                    .map(UserResponse::fromUser);
            return ResponseEntity.ok(usersPage);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .error(e.getMessage()).build());
        }
    }
}
