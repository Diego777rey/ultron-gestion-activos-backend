package com.dev.ultron.service.personas;

import com.dev.ultron.domain.personas.Funcionario;
import com.dev.ultron.domain.personas.Role;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.domain.personas.UsuarioRole;
import com.dev.ultron.domain.personas.UsuarioRoleId;
import com.dev.ultron.dto.personas.input.UsuarioInput;
import com.dev.ultron.dto.personas.mapper.UsuarioMapper;
import com.dev.ultron.dto.personas.output.UsuarioOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.SearchNormalizer;
import com.dev.ultron.repository.personas.FuncionarioRepository;
import com.dev.ultron.repository.personas.RoleRepository;
import com.dev.ultron.repository.personas.UsuarioRepository;
import com.dev.ultron.repository.personas.UsuarioRoleRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService extends GenericCrudService<Usuario, Long> {

    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final RoleRepository roleRepository;
    private final UsuarioRoleRepository usuarioRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          FuncionarioRepository funcionarioRepository,
                          RoleRepository roleRepository,
                          UsuarioRoleRepository usuarioRoleRepository,
                          PasswordEncoder passwordEncoder,
                          UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.roleRepository = roleRepository;
        this.usuarioRoleRepository = usuarioRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    protected JpaRepository<Usuario, Long> getRepository() {
        return usuarioRepository;
    }

    @Transactional
    public UsuarioOutput registrarUsuario(UsuarioInput input) {
        validarInput(input, null);
        String encodedPassword = passwordEncoder.encode(input.password());
        Usuario usuario = usuarioMapper.toEntity(input, encodedPassword);
        usuario.setFuncionario(resolverFuncionario(input.id_funcionario(), null));
        usuario = guardar(usuario);
        asignarRoles(usuario, input.roleIds());
        return usuarioMapper.toOutput(recargar(usuario.getId()));
    }

    @Transactional
    public UsuarioOutput actualizarUsuario(Long id, UsuarioInput input) {
        Usuario usuario = buscarPorIdOrThrow(id);
        validarInput(input, id);
        usuarioMapper.updateEntity(usuario, input);
        if (input.password() != null && !input.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(input.password()));
        }
        usuario.setFuncionario(resolverFuncionario(input.id_funcionario(), id));
        usuario = actualizar(usuario);
        reemplazarRoles(usuario, input.roleIds());
        return usuarioMapper.toOutput(recargar(usuario.getId()));
    }

    @Transactional(readOnly = true)
    public List<UsuarioOutput> listarTodosUsuarios() {
        return usuarioRepository.findAllWithRolesAndFuncionario().stream()
                .map(usuarioMapper::toOutput)
                .toList();
    }

    @Transactional(readOnly = true)
    public com.dev.ultron.generic.PageResponse<UsuarioOutput> listarUsuariosPaginado(int page, int size) {
        org.springframework.data.domain.Page<Usuario> pagina = listarPaginado(
            org.springframework.data.domain.PageRequest.of(page, size)
        );
        return new com.dev.ultron.generic.PageResponse<>(
            pagina.map(usuarioMapper::toOutput)
        );
    }

    @Transactional(readOnly = true)
    public UsuarioOutput buscarUsuarioPorId(Long id) {
        return usuarioMapper.toOutput(recargar(id));
    }

    @Transactional
    public boolean eliminarUsuario(Long id) {
        usuarioRoleRepository.deleteByUsuario_Id(id);
        eliminarPorId(id);
        return true;
    }

    private void validarInput(UsuarioInput input, Long usuarioId) {
        if (input.username() == null || input.username().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (usuarioId == null && (input.password() == null || input.password().isBlank())) {
            throw new IllegalArgumentException("La contraseña es obligatoria al registrar un usuario");
        }
        if (input.id_funcionario() == null) {
            throw new IllegalArgumentException("Debe seleccionar un funcionario para el usuario");
        }
        String username = SearchNormalizer.normalize(input.username());
        boolean usernameDuplicado = usuarioId == null
                ? usuarioRepository.existsByUsername(username)
                : usuarioRepository.existsByUsernameAndIdNot(username, usuarioId);
        if (usernameDuplicado) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre");
        }
    }

    private Funcionario resolverFuncionario(Long idFuncionario, Long usuarioId) {
        Funcionario funcionario = funcionarioRepository.findById(idFuncionario)
                .orElseThrow(() -> new IllegalArgumentException("Funcionario no encontrado"));
        boolean funcionarioOcupado = usuarioId == null
                ? usuarioRepository.existsByFuncionarioId(idFuncionario)
                : usuarioRepository.existsByFuncionarioIdAndIdNot(idFuncionario, usuarioId);
        if (funcionarioOcupado) {
            throw new IllegalArgumentException("El funcionario seleccionado ya tiene un usuario asignado");
        }
        return funcionario;
    }

    private void asignarRoles(Usuario usuario, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + roleId));
            UsuarioRole usuarioRole = UsuarioRole.builder()
                    .id(UsuarioRoleId.builder()
                            .usuario_id(usuario.getId())
                            .role_id(role.getId())
                            .build())
                    .usuario(usuario)
                    .role(role)
                    .build();
            usuarioRoleRepository.save(usuarioRole);
        }
    }

    private void reemplazarRoles(Usuario usuario, List<Long> roleIds) {
        usuarioRoleRepository.deleteByUsuario_Id(usuario.getId());
        asignarRoles(usuario, roleIds);
    }

    private Usuario recargar(Long id) {
        return usuarioRepository.findByIdWithRolesAndFuncionario(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional
    public UsuarioOutput agregarRolAUsuario(Long usuarioId, Long roleId) {
        Usuario usuario = buscarPorIdOrThrow(usuarioId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + roleId));

        boolean yaAsignado = usuarioRoleRepository
                .existsById(UsuarioRoleId.builder()
                        .usuario_id(usuarioId)
                        .role_id(roleId)
                        .build());
        if (yaAsignado) {
            throw new IllegalArgumentException("El rol ya está asignado a este usuario");
        }

        UsuarioRole usuarioRole = UsuarioRole.builder()
                .id(UsuarioRoleId.builder()
                        .usuario_id(usuario.getId())
                        .role_id(role.getId())
                        .build())
                .usuario(usuario)
                .role(role)
                .build();
        usuarioRoleRepository.save(usuarioRole);
        return usuarioMapper.toOutput(recargar(usuarioId));
    }

    @Transactional
    public UsuarioOutput quitarRolDeUsuario(Long usuarioId, Long roleId) {
        buscarPorIdOrThrow(usuarioId);
        UsuarioRoleId urId = UsuarioRoleId.builder()
                .usuario_id(usuarioId)
                .role_id(roleId)
                .build();
        if (!usuarioRoleRepository.existsById(urId)) {
            throw new IllegalArgumentException("El usuario no tiene asignado ese rol");
        }
        usuarioRoleRepository.deleteById(urId);
        return usuarioMapper.toOutput(recargar(usuarioId));
    }
}
