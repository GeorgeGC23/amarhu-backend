package com.amarhu.user.service;

import com.amarhu.user.dto.UserRequestDTO;
import com.amarhu.user.entity.Role;
import com.amarhu.user.entity.User;
import com.amarhu.user.entity.UserRequest;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.user.repository.UserRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserRequestService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRequestRepository userRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.backend.url}")
    private String baseUrl;

    public void createUserRequest(UserRequestDTO userRequestDTO) {
        if (userRequestRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Ya existe una solicitud con este correo.");
        }

        // Crear una nueva solicitud con un token único
        UserRequest request = new UserRequest();
        request.setName(userRequestDTO.getName());
        request.setEmail(userRequestDTO.getEmail());
        request.setRequestedRole(userRequestDTO.getRequestedRole());
        request.setToken(UUID.randomUUID().toString()); // Generar token único
        request.setCreatedAt(LocalDateTime.now()); // Asignar valor al campo createdAt
        userRequestRepository.save(request);

        // Notificar a los directivos
        notifyDirectives(request);
    }

    public void approveRequest(Long requestId, String token) {
        UserRequest request = userRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));

        if (!request.getToken().equals(token)) {
            throw new IllegalArgumentException("Token inválido. No se puede aprobar esta solicitud.");
        }

        if (request.isApproved()) {
            throw new IllegalStateException("La solicitud ya fue aprobada.");
        }

        // Generar una contraseña aleatoria
        String rawPassword = generateRandomPassword(10);  // Por ejemplo, longitud de 10 caracteres
        String hashedPassword = passwordEncoder.encode(rawPassword);  // Hashea la contraseña

        // Generar el código de usuario único
        String generatedCode = generateUserCode(request.getRequestedRole());

        // Crear y guardar el nuevo usuario
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setRole(request.getRequestedRole());
        newUser.setPassword(hashedPassword);  // Guardar la contraseña hasheada
        newUser.setCodigo(generatedCode);
        newUser.setActive(true);
        userRepository.save(newUser);

        // Marcar la solicitud como aprobada
        request.setApproved(true);
        request.setApprovedAt(LocalDateTime.now());
        userRequestRepository.save(request);

        // Enviar un correo de confirmación al usuario con la contraseña deshasheada
        sendConfirmationEmail(newUser.getEmail(), rawPassword);
    }

    private void sendConfirmationEmail(String email, String password) {
        String subject = "Bienvenido a Amarhu - Confirmación de Registro";
        String body = String.format(
                "Estimado usuario,\n\n" +
                        "Su cuenta ha sido creada exitosamente. A continuación, se detallan sus credenciales de acceso:\n\n" +
                        "Email: %s\n" +
                        "Contraseña: %s\n\n" +
                        "Por favor, cambie su contraseña al iniciar sesión por primera vez.\n\n" +
                        "Gracias por unirse a nuestra plataforma.\n\n" +
                        "Atentamente,\nEl equipo de Amarhu",
                email, password
        );

        emailService.sendEmail(email, subject, body);
    }

    private void notifyDirectives(UserRequest request) {
        // Obtener todos los usuarios con rol DIRECTIVO
        List<User> directives = userRepository.findByRole(Role.DIRECTIVO);

        if (directives.isEmpty()) {
            throw new IllegalStateException("No se encontraron directivos registrados en el sistema.");
        }

        String subject = "Nueva Solicitud de Usuario";
        String bodyTemplate = "Se ha recibido una nueva solicitud de usuario:\n\n" +
                "Nombre: %s\nCorreo: %s\nRol solicitado: %s\n\n" +
                "Para aprobar esta solicitud, haga clic en el botón de abajo:\n\n" +
                "<a href=\"%s\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: white; " +
                "background-color: #4CAF50; text-decoration: none; border-radius: 5px;\">Aprobar Solicitud</a>\n\n" +
                "Gracias.";

        // URL de aprobación con el token
        String approveUrl = String.format("%s/api/user-requests/%d/approve?token=%s",
                baseUrl, request.getId(), request.getToken());

        // Enviar un correo a cada directivo
        directives.forEach(directive -> {
            String body = String.format(bodyTemplate, request.getName(), request.getEmail(), request.getRequestedRole(), approveUrl);
            emailService.sendEmail(directive.getEmail(), subject, body);
        });
    }

    private String generateRandomPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
    private String generateUserCode(Role role) {
        String prefix;
        switch (role) {
            case REDACTOR:
                prefix = "RA90";
                break;
            case LOCUTOR:
                prefix = "L";
                break;
            case EDITOR:
                prefix = "E";
                break;
            case PANELISTA:
                prefix = "PAN";
                break;
            default:
                throw new IllegalArgumentException("Rol no soportado para la generación de código.");
        }

        int nextNumber = 1;
        while (true) {
            String code = prefix + String.format("%02d", nextNumber);
            if (!userRepository.existsByCodigo(code)) {
                return code;
            }
            nextNumber++;
        }
    }
}
