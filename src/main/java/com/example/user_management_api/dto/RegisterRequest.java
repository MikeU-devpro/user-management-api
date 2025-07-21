package com.example.user_management_api.dto; // Corrected package name

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@Builder // Lombok: Provides a builder pattern for object creation
@AllArgsConstructor // Lombok: Generates a constructor with all fields
@NoArgsConstructor // Lombok: Generates a no-argument constructor
public class RegisterRequest {

    private String name;
    private String email;
    private String password;
}