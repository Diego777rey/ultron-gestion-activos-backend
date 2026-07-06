package com.dev.ultron.service.security;

import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.repository.personas.UsuarioRepository;
import com.dev.ultron.utilitarios.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = StringUtil.normalizeUsername(username);
        Usuario usuario = usuarioRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + normalizedUsername));

        return org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .authorities(usuario.getUsuarioRoles().stream()
                        .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getDescripcion()))
                        .collect(Collectors.toList()))
                .build();
    }
}
