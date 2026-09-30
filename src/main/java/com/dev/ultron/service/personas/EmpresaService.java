package com.dev.ultron.service.personas;

import com.dev.ultron.domain.personas.Empresa;
import com.dev.ultron.dto.personas.input.EmpresaInput;
import com.dev.ultron.dto.personas.mapper.EmpresaMapper;
import com.dev.ultron.dto.personas.output.EmpresaOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.repository.personas.EmpresaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Empresa para gestión de empresas del sistema.
 */
@Service
public class EmpresaService extends GenericCrudService<Empresa, Long> {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    public EmpresaService(EmpresaRepository empresaRepository, EmpresaMapper empresaMapper) {
        this.empresaRepository = empresaRepository;
        this.empresaMapper = empresaMapper;
    }

    @Override
    protected JpaRepository<Empresa, Long> getRepository() {
        return empresaRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Empresa empresa) {
        if (empresa.getRazon_social() == null || empresa.getRazon_social().isEmpty()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        if (empresa.getRuc() == null || empresa.getRuc().isEmpty()) {
            throw new IllegalArgumentException("El RUC es obligatorio");
        }
        if (empresa.getDireccion() == null || empresa.getDireccion().isEmpty()) {
            throw new IllegalArgumentException("La dirección es obligatoria");
        }
    }

    /**
     * Registra una nueva empresa.
     */
    @Transactional
    public EmpresaOutput registrarEmpresa(EmpresaInput input) {
        Empresa empresa = empresaMapper.toEntity(input);
        empresa = guardar(empresa);
        return empresaMapper.toOutput(empresa);
    }

    /**
     * Actualiza una empresa existente.
     */
    @Transactional
    public EmpresaOutput actualizarEmpresa(Long id, EmpresaInput input) {
        Empresa empresa = buscarPorIdOrThrow(id);
        empresaMapper.updateEntity(empresa, input);
        empresa = actualizar(empresa);
        return empresaMapper.toOutput(empresa);
    }

    /**
     * Lista todas las empresas como output DTOs.
     */
    @Transactional(readOnly = true)
    public List<EmpresaOutput> listarTodasEmpresas() {
        return empresaRepository.findAll()
                .stream()
                .map(empresaMapper::toOutput)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una empresa por ID como output DTO.
     */
    @Transactional(readOnly = true)
    public EmpresaOutput obtenerEmpresaPorId(Long id) {
        Empresa empresa = buscarPorIdOrThrow(id);
        return empresaMapper.toOutput(empresa);
    }
}
