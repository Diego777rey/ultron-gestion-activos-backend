package com.dev.ultron.service.personas;

import com.dev.ultron.domain.personas.Role;
import com.dev.ultron.dto.personas.input.RoleInput;
import com.dev.ultron.dto.personas.mapper.RoleMapper;
import com.dev.ultron.dto.personas.output.RoleOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.SearchNormalizer;
import com.dev.ultron.repository.personas.RoleRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService extends GenericCrudService<Role, Long> {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleService(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    protected JpaRepository<Role, Long> getRepository() {
        return roleRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Role role) {
        if (role.getDescripcion() == null || role.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción del rol es obligatoria");
        }
    }

    @Transactional
    public RoleOutput registrarRole(RoleInput input) {
        Role role = roleMapper.toEntity(input);
        role = guardar(role);
        return roleMapper.toOutput(role);
    }

    @Transactional
    public RoleOutput actualizarRole(Long id, RoleInput input) {
        Role role = buscarPorIdOrThrow(id);
        roleMapper.updateEntity(role, input);
        role = actualizar(role);
        return roleMapper.toOutput(role);
    }

    @Transactional(readOnly = true)
    public List<RoleOutput> listarTodosRoles() {
        return listarTodos().stream()
                .map(roleMapper::toOutput)
                .toList();
    }

    @Transactional(readOnly = true)
    public com.dev.ultron.generic.PageResponse<RoleOutput> listarRolesPaginado(int page, int size) {
        org.springframework.data.domain.Page<Role> pagina = listarPaginado(
            org.springframework.data.domain.PageRequest.of(page, size)
        );
        return new com.dev.ultron.generic.PageResponse<>(
            pagina.map(roleMapper::toOutput)
        );
    }

    @Transactional(readOnly = true)
    public com.dev.ultron.generic.PageResponse<RoleOutput> rolesUsuarioPaginado(Long usuarioId, int page, int size, String filter) {
        org.springframework.data.domain.Page<Role> pagina = roleRepository.findRolesByUsuarioIdPaginado(
            usuarioId, SearchNormalizer.normalizeFilter(filter), org.springframework.data.domain.PageRequest.of(page, size)
        );
        return new com.dev.ultron.generic.PageResponse<>(
            pagina.map(roleMapper::toOutput)
        );
    }

    @Transactional(readOnly = true)
    public com.dev.ultron.generic.PageResponse<RoleOutput> rolesDisponiblesUsuarioPaginado(Long usuarioId, int page, int size, String filter) {
        org.springframework.data.domain.Page<Role> pagina = roleRepository.findRolesDisponiblesByUsuarioIdPaginado(
            usuarioId, SearchNormalizer.normalizeFilter(filter), org.springframework.data.domain.PageRequest.of(page, size)
        );
        return new com.dev.ultron.generic.PageResponse<>(
            pagina.map(roleMapper::toOutput)
        );
    }

    @Transactional(readOnly = true)
    public RoleOutput buscarRolePorId(Long id) {
        return roleMapper.toOutput(buscarPorIdOrThrow(id));
    }

    @Transactional
    public boolean eliminarRole(Long id) {
        eliminarPorId(id);
        return true;
    }
}
