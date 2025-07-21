package com.example.user_management_api.auth; // Corrected package name

import com.example.user_management_api.dto.AuthenticationResponse; // Import DTOs
import com.example.user_management_api.dto.LoginRequest;
import com.example.user_management_api.dto.RegisterRequest;
import com.example.user_management_api.model.User; // Import User model
import com.example.user_management_api.repository.UserRepository; // Import UserRepository
import com.example.user_management_api.service.JwtService; // Import JwtService
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException; // For handling unique email constraint
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // Marks this class as a Spring service component
@RequiredArgsConstructor // Lombok: Generates a constructor with all final fields for dependency injection
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Injected password encoder
    private final JwtService jwtService; // Injected JWT service
    private final AuthenticationManager authenticationManager; // Injected authentication manager

    // Method to handle user registration
    public AuthenticationResponse register(RegisterRequest request) {
        // Optional: Check if user with this email already exists to provide a friendlier error
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered. Please use a different email.");
            // In a real application, you'd use a custom exception and a proper error handling mechanism
        }

        // Build a new User entity from the registration request
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Encode the password before saving!
                .build();

        // Save the new user to the database
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Catch if unique email constraint somehow fails (e.g., race condition)
            throw new RuntimeException("Error registering user: Email already in use.", e);
        }

        // Generate a JWT for the newly registered user
        var jwtToken = jwtService.generateToken(user);

        // Return the JWT in an AuthenticationResponse
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    // Method to handle user authentication (login)
    public AuthenticationResponse authenticate(LoginRequest request) {
        // Attempt to authenticate the user using Spring Security's AuthenticationManager
        // This will trigger our UserDetailsService and PasswordEncoder
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),    // The "username" (email)
                        request.getPassword()  // The raw password
                )
        );

        // If authentication succeeds, load the UserDetails (our User entity)
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after successful authentication. This should not happen.")); // Should not happen if authentication passed

        // Generate a JWT for the authenticated user
        var jwtToken = jwtService.generateToken(user);

        // Return the JWT in an AuthenticationResponse
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}