package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.dto.AuthDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.DuplicateResourceException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.User;
import com.demo.staffing_management_backend.model.enums.UserRole;
import com.demo.staffing_management_backend.repository.UserRepository;
import com.demo.staffing_management_backend.security.JwtService;
import com.demo.staffing_management_backend.security.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already taken " + request.email());
        }
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.EMPLOYEE)
                .enabled(true)
                .tokenVersion(0)
                .build();
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        if (loginAttemptService.isBlocked(request.username())) {
            throw new BadRequestException("Too many failed login attempts. Please try again later.");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (BadCredentialsException ex) {
            loginAttemptService.recordFailure(request.username());
            throw ex;
        }
        loginAttemptService.reset(request.username());

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found " + request.username()));
        return buildAuthResponse(user);
    }

    public AuthDtos.AuthResponse refresh(AuthDtos.RefreshRequest request) {
        String token = request.refreshToken();
        try {
            String username = jwtService.extractUsername(token);
            if (!JwtService.TYPE_REFRESH.equals(jwtService.extractTokenType(token))) {
                throw new BadRequestException("Provided token is not a refresh token");
            }
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Integer version = jwtService.extractTokenVersion(token);
            if (!jwtService.isTokenValid(token, userDetails) || !user.isEnabled()
                    || version == null || version != user.getTokenVersion()) {
                throw new BadRequestException("Invalid or expired refresh token");
            }
            return buildAuthResponse(user);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Invalid or expired refresh token");
        }
    }

    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        });
    }

    private AuthDtos.AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtService.generateAccessToken(userDetails, user.getTokenVersion(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getTokenVersion());
        return new AuthDtos.AuthResponse(accessToken, refreshToken, user.getUsername(), user.getRole());
    }
}
