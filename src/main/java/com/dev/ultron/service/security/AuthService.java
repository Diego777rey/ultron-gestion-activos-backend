package com.dev.ultron.service.security;

import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.repository.personas.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Usuario getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("Debe iniciar sesión");
        }
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario logueado"));
    }
}
