package com.amarhu.user.service;

import com.amarhu.security.jwt.JwtUtil;
import com.amarhu.user.dto.AuthResponse;
import com.amarhu.user.dto.UserDTO;
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
    public AuthResponse authenticate(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            System.out.println("Usuario encontrado: " + user.getEmail());
            System.out.println("Contraseña encriptada almacenada: " + user.getPassword());

            if (passwordEncoder.matches(password, user.getPassword())) {
                String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

                // Convertir a DTO
                UserDTO userDTO = new UserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().toString(),
                        user.getCodigo(),
                        user.getAvatar(),
                        user.getCreatedAt(),
                        user.isActive()
                );

                // Retornar el DTO y el JWT juntos
                return new AuthResponse(jwt, userDTO);
            } else {
                System.out.println("❌ Error en la validación de la contraseña.");
                throw new RuntimeException("Invalid credentials");
            }
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }


}
