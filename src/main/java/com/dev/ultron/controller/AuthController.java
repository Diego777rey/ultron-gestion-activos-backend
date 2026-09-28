package com.dev.ultron.controller;

import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.security.AuthRequest;
import com.dev.ultron.dto.security.AuthResponse;
import com.dev.ultron.dto.security.RoleDto;
import com.dev.ultron.dto.security.PermisoDto;
import com.dev.ultron.repository.personas.UsuarioRepository;
import com.dev.ultron.service.security.TokenService;
import com.dev.ultron.utilitarios.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        if (StringUtil.isNullOrEmpty(request.getUsername()) || StringUtil.isNullOrEmpty(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String username = StringUtil.normalizeUsername(request.getUsername());
        String password = request.getPassword().trim();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        String token = tokenService.generateToken(authentication);

        Usuario usuario = usuarioRepository.findByUsernameWithRolesAndPermissions(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var roles = usuario.getUsuarioRoles().stream()
                .map(ur -> RoleDto.builder()
                        .id(ur.getRole().getId())
                        .descripcion(ur.getRole().getDescripcion())
                        .permisos(ur.getRole().getRolePermisos().stream()
                                .map(rp -> PermisoDto.builder()
                                        .id(rp.getPermiso().getId())
                                        .modulo(rp.getPermiso().getModulo())
                                        .accion(rp.getPermiso().getAccion())
                                        .descripcion(rp.getPermiso().getDescripcion())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(authentication.getName())
                .roles(roles)
                .build());
    }
}
