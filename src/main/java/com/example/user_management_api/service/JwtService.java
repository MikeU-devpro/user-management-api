package com.example.user_management_api.service; // Corrected package name

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service // Marks this class as a Spring service component
public class JwtService {

    // Inject the JWT secret key from application.properties
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Inject the JWT expiration time from application.properties (in milliseconds)
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // --- Token Generation ---

    // Generates a JWT for a given UserDetails
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Generates a JWT with extra claims
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims) // Add any extra claims
                .setSubject(userDetails.getUsername()) // Set subject (username/email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Set issued at time
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Set expiration time
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Sign the token with the secret key and algorithm
                .compact(); // Build and compact the token into a string
    }

    // --- Token Validation & Extraction ---

    // Checks if a token is valid for a given UserDetails
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Extracts the username (email) from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extracts a specific claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extracts all claims (payload) from the token
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey()) // Set the signing key for parsing
                .build()
                .parseClaimsJws(token) // Parse the token
                .getBody(); // Get the claims (payload)
    }

    // Checks if the token has expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extracts the expiration date from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // --- Secret Key Management ---

    // Decodes the base64 encoded secret key and returns it as a Key object
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}