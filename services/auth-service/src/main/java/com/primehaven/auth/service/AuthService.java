package com.primehaven.auth.service;

import com.primehaven.auth.dto.AuthResponse;
import com.primehaven.auth.dto.LoginRequest;
import com.primehaven.auth.dto.RegisterRequest;
import com.primehaven.auth.dto.TokenValidationResponse;
import com.primehaven.auth.entity.User;
import com.primehaven.auth.exception.BadRequestException;
import com.primehaven.auth.exception.UnauthorizedException;
import com.primehaven.auth.repository.UserRepository;
import com.primehaven.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        String role = request.getRole();
        if (role == null || role.isBlank()) {
            role = "ROLE_BUYER";
        } else {
            role = role.trim().toUpperCase();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole());

        return new AuthResponse(
                token,
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                jwtUtil.getExpirationMs()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                jwtUtil.getExpirationMs()
        );
    }

    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new TokenValidationResponse(false, null, null, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();
        if (!jwtUtil.validateToken(token)) {
            return new TokenValidationResponse(false, null, null, "Token is expired or invalid");
        }

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        return new TokenValidationResponse(true, username, role, "Token is valid");
    }
}
