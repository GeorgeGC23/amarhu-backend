package com.amarhu.user.service;

import com.amarhu.security.jwt.JwtUtil;
import com.amarhu.user.entity.User;
import com.amarhu.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Autentica al usuario y genera un token JWT si es exitoso.
     *
     * @param email    El email del usuario.
     * @param password La contraseña sin encriptar (provista en el request).
     * @return El token JWT si la autenticación es exitosa.
     */
    public String authenticate(String email, String password) {
        // Busca al usuario por email
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Verifica la contraseña encriptada
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Genera el token JWT usando el email del usuario como subject
                return jwtUtil.generateToken(user.getEmail());
            } else {
                throw new RuntimeException("Invalid credentials");
            }
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }
}
