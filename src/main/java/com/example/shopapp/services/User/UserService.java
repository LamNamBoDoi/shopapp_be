package com.example.shopapp.services.User;

import com.example.shopapp.components.JwtTokenUtils;
import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.RefreshTokenDTO;
import com.example.shopapp.dtos.UpdateUserDTO;
import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.PermissionDenyException;
import com.example.shopapp.models.Role;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.RoleRepository;
import com.example.shopapp.repositories.TokenRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.response.LoginResponse;
import com.example.shopapp.services.FileStorageService.FileStorageService;
import com.example.shopapp.services.Token.TokenService;
import com.example.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService extends TranslateMessages implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final TokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final FileStorageService fileStorageService;

    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        // Kiểm tra xem số điện thoại đã tồn tại chưa
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));
        if (role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new PermissionDenyException("You cannot register an admin account");
        }
        String thumbnail = null;
        if (userDTO.getThumbnail() != null && !userDTO.getThumbnail().isEmpty()) {
            thumbnail = fileStorageService.storeFile(userDTO.getThumbnail());
        }
        // convert userdto sang user
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
                .thumbnail(thumbnail)
                .password(userDTO.getPassword())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();

        newUser.setRole(role);
        // Kiểm tra nếu có accountId, không yêu cầu password
        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
            String password = userDTO.getPassword();
            String encoderPassword = passwordEncoder.encode(password);
            newUser.setPassword(encoderPassword);
        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("Invalid phonenumber / password");
        }
        User existingUser = optionalUser.get();
        // check password
        if (existingUser.getFacebookAccountId() == 0 && existingUser.getGoogleAccountId() == 0) {
            if (!passwordEncoder.matches(password, existingUser.getPassword())) {
                throw new BadCredentialsException("Wrong phone number or password");
            }
        }
        // authenticate with java spring security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, password,
                existingUser.getAuthorities()
        );
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtils.generateToken(existingUser);
//      return optionalUser.get();//muon tra ve JWT token
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if (jwtTokenUtils.isTokenExpired(token)) {
            throw new Exception(translate(MessageKeys.TOKEN_EXPIRATION_TIME));
        }

        String phoneNumber = jwtTokenUtils.extractPhonenumber(token);
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);

        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new DataNotFoundException(translate(MessageKeys.USER_NOT_FOUND));
        }
    }

    @Override
    public User updateUser(Long userId, UpdateUserDTO updateUserDTO) throws Exception {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(translate(MessageKeys.USER_NOT_FOUND)));

        String phoneNumber = updateUserDTO.getPhoneNumber();
        if (!existingUser.getPhoneNumber().equals(phoneNumber)
                && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException(translate(MessageKeys.PASSWORD_NOT_MATCH));
        }

        if (updateUserDTO.getFullName() != null) {
            existingUser.setFullName(updateUserDTO.getFullName());
        }
        if (updateUserDTO.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updateUserDTO.getPhoneNumber());
        }
        if (updateUserDTO.getAddress() != null) {
            existingUser.setAddress(updateUserDTO.getAddress());
        }
        if (updateUserDTO.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updateUserDTO.getDateOfBirth());
        }
        if (updateUserDTO.getFacebookAccountId() > 0) {
            existingUser.setFacebookAccountId(updateUserDTO.getFacebookAccountId());
        }
        if (updateUserDTO.getGoogleAccountId() > 0) {
            existingUser.setGoogleAccountId(updateUserDTO.getGoogleAccountId());
        }
        // cập nhật mật khẩu
        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isEmpty()) {
            String newPassword = passwordEncoder.encode(updateUserDTO.getPassword());
            existingUser.setPassword(newPassword);
        }
        if(updateUserDTO.getThumbnail() != null && !updateUserDTO.getThumbnail().isEmpty()){
            fileStorageService.deleteFile(existingUser.getThumbnail());
            String thumbnailUrl = fileStorageService.storeFile(updateUserDTO.getThumbnail());
            existingUser.setThumbnail(thumbnailUrl);
        }
        return userRepository.save(existingUser);
    }

    @Override
    public Page<User> findAllUsers(String keyword, Pageable pageable) {
        return userRepository.findAll(keyword, pageable);
    }

    // dùng refresh_token để tạo lại token mới
    @Override
    public LoginResponse refreshToken(RefreshTokenDTO refreshTokenDTO) throws DataNotFoundException, PermissionDenyException {
        Token token = refreshTokenService.verifyRefreshToken(refreshTokenDTO.getRefreshToken());

        // kiểm tra refreshToken còn hạn không
        if (token.getRefreshExpirationTime().isBefore(Instant.now())) {
            throw new PermissionDenyException(translate(MessageKeys.APP_PERMISSION_DENY_EXCEPTION));
        }

        // tạo token mới bằng refreshToken
        return LoginResponse.builder()
                .token(jwtTokenUtils.generateToken(token.getUser()))
                .refreshToken(token.getRefreshToken())
                .build();
    }


    @Override
    public void resetPassword(Long userId, String newPassword) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(translate(MessageKeys.USER_NOT_FOUND))
        );

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        //reset token
        List<Token> tokens = tokenRepository.findByUser(user);
        tokenRepository.deleteAll(tokens);
    }

    @Override
    public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(translate(MessageKeys.USER_NOT_FOUND))
        );
        user.setActive(active);
        userRepository.save(user);
    }

    @Override
    public List<User> findAllUsersNoPage() {
        return userRepository.findAll();
    }


}
