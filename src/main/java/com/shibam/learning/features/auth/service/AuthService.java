package com.shibam.learning.features.auth.service;

import com.shibam.learning.common.security.JwtTokenProvider;
import com.shibam.learning.common.utils.RoleName;
import com.shibam.learning.features.auth.dto.req.LoginRequest;
import com.shibam.learning.features.auth.dto.req.SignupRequest;
import com.shibam.learning.features.auth.dto.req.TokenRefreshRequest;
import com.shibam.learning.features.auth.dto.res.JwtResponse;
import com.shibam.learning.features.auth.dto.res.TokenRefreshResponse;
import com.shibam.learning.features.auth.entity.RefreshToken;
import com.shibam.learning.features.auth.entity.Role;
import com.shibam.learning.features.auth.entity.User;
import com.shibam.learning.features.auth.repository.RefreshTokenRepository;
import com.shibam.learning.features.auth.repository.RoleRepository;
import com.shibam.learning.features.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@Transactional
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public ResponseEntity<?> register(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already in use!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign USER role only, ignore roles from SignupRequest
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER not found."));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    public ResponseEntity<?> createModerator(SignupRequest request) {
        return createUserWithRole(request, RoleName.ROLE_MODERATOR, "Moderator registered successfully!");
    }

    public ResponseEntity<?> createUserByAdmin(SignupRequest request) {
        return createUserWithRole(request, RoleName.ROLE_USER, "User registered by admin successfully!");
    }

    public ResponseEntity<?> createUserByModerator(SignupRequest request) {
        return createUserWithRole(request, RoleName.ROLE_USER, "User registered by moderator successfully!");
    }

    private ResponseEntity<?> createUserWithRole(SignupRequest request, RoleName roleName, String successMessage) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already in use!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " not found."));
        user.setRoles(Set.of(role));

        userRepository.save(user);
        return ResponseEntity.ok(successMessage);
    }


    public ResponseEntity<?> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElse(null);

        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setUser(user);
        }
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()));

        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshTokenStr, "Bearer"));
    }

    public ResponseEntity<?> refreshAccessToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshToken -> {
                    if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
                        refreshTokenRepository.delete(refreshToken);
                        return ResponseEntity.badRequest().body("Refresh token expired. Please login again.");
                    }

                    String username = jwtTokenProvider.getUsernameFromToken(requestRefreshToken);
                    String newAccessToken = jwtTokenProvider.generateToken(
                            new UsernamePasswordAuthenticationToken(username, null, null));
                    return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, "Bearer"));
                }).orElse(ResponseEntity.badRequest().body("Refresh token not found."));
    }

    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            refreshTokenRepository.deleteByUser(user);
        });
    }
}
