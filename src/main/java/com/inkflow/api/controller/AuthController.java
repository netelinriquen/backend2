package com.inkflow.api.controller;

import com.inkflow.api.dto.AuthResponse;
import com.inkflow.api.dto.LoginRequest;
import com.inkflow.api.entity.User;
import com.inkflow.api.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getSenha(), user.getSenha())) {
            return ResponseEntity.badRequest().body("Senha incorreta");
        }

        String token = "fake-jwt-token-" + user.getId();
        
        return ResponseEntity.ok(new AuthResponse(token, 
            new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getIsAdmin())));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email já cadastrado");
        }

        user.setSenha(passwordEncoder.encode(user.getSenha()));
        user.setIsAdmin(false);
        
        User savedUser = userRepository.save(user);
        String token = "fake-jwt-token-" + savedUser.getId();
        
        return ResponseEntity.ok(new AuthResponse(token,
            new UserResponse(savedUser.getId(), savedUser.getNome(), savedUser.getEmail(), savedUser.getIsAdmin())));
    }

    static class UserResponse {
        private Long id;
        private String nome;
        private String email;
        private Boolean isAdmin;

        public UserResponse(Long id, String nome, String email, Boolean isAdmin) {
            this.id = id;
            this.nome = nome;
            this.email = email;
            this.isAdmin = isAdmin;
        }

        public Long getId() { return id; }
        public String getNome() { return nome; }
        public String getEmail() { return email; }
        public Boolean getIsAdmin() { return isAdmin; }
    }
}