package com.demo.staffing_management_backend.service;

import com.demo.staffing_management_backend.dto.AuthDtos;
import com.demo.staffing_management_backend.exception.BadRequestException;
import com.demo.staffing_management_backend.exception.DuplicateResourceException;
import com.demo.staffing_management_backend.exception.ResourceNotFoundException;
import com.demo.staffing_management_backend.model.User;
import com.demo.staffing_management_backend.model.enums.UserRole;
import com.demo.staffing_management_backend.repository.UserRepository;
import com.demo.staffing_management_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if(request.username()==null || request.username().isBlank()){
            throw new BadRequestException("Username is required");
        }
        if(request.password()==null || request.password().isBlank()){
            throw new BadRequestException("Password is required");
        }
        if(request.email()==null || request.email().isBlank()){
            throw new BadRequestException("Email is required");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken "+request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already taken "+request.email());
        }
        User user= User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.EMPLOYEE)
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);
        String token=generateTokenForUsername(user.getUsername(),user.getRole());
        return new AuthDtos.AuthResponse(token,user.getUsername(),user.getRole());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        if(request.username()==null || request.password()==null){
            throw new BadRequestException("Username and password are required");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(),request.password()));

        User user=userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found "+request.username()));

        String token=generateTokenForUsername(user.getUsername(),user.getRole());

        return new AuthDtos.AuthResponse(token,user.getUsername(),user.getRole());

    }

    private String generateTokenForUsername(String username, UserRole role) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Map<String,Object> claims = new HashMap<>();
        claims.put("role",role.name());
        return jwtService.generateToken(claims,userDetails);
    }
}
