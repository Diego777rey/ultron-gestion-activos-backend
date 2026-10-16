package com.dev.ultron.service.compras;

import com.dev.ultron.domain.compras.Proveedor;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.compras.input.ProveedorInput;
import com.dev.ultron.dto.compras.mapper.ProveedorMapper;
import com.dev.ultron.dto.compras.output.ProveedorOutput;
import com.dev.ultron.dto.personas.input.PersonaInput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.repository.compras.ProveedorRepository;
import com.dev.ultron.service.personas.PersonaService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de Proveedor para gestión de proveedores del sistema.
 */
@Service
public class ProveedorService extends GenericCrudService<Proveedor, Long> {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;
    private final PersonaService personaService;

    public ProveedorService(ProveedorRepository proveedorRepository,
                            ProveedorMapper proveedorMapper,
                            PersonaService personaService) {
        this.proveedorRepository = proveedorRepository;
        this.proveedorMapper = proveedorMapper;
        this.personaService = personaService;
    }

    @Override
    protected JpaRepository<Proveedor, Long> getRepository() {
        return proveedorRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Proveedor proveedor) {
        if (proveedor.getTipo_persona() == null) {
            throw new IllegalArgumentException("El tipo de persona es obligatorio");
        }

        if (proveedor.getRuc() == null || proveedor.getRuc().isEmpty()) {
            throw new IllegalArgumentException("El RUC es obligatorio");
        }

        if (proveedor.getTipo_persona() == Proveedor.TipoPersona.FISICA) {
            if (proveedor.getApellido() == null) {
                proveedor.setApellido("");
            }
            if (proveedor.getNombre() == null || proveedor.getNombre().isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio para persona física");
            }
        } else if (proveedor.getTipo_persona() == Proveedor.TipoPersona.JURIDICA) {
            if (proveedor.getRazon_social() == null || proveedor.getRazon_social().isEmpty()) {
                throw new IllegalArgumentException("La razón social es obligatoria para persona jurídica");
            }
        }
    }

    /**
     * Registra un nuevo proveedor.
     */
    @Transactional
    public ProveedorOutput registrarProveedor(ProveedorInput input) {
        Proveedor proveedor = proveedorMapper.toEntity(input);
        vincularPersona(proveedor, input);
        proveedor = guardar(proveedor);
        return proveedorMapper.toOutput(proveedor);
    }

    @Transactional
    public ProveedorOutput actualizarProveedor(Long id, ProveedorInput input) {
        Proveedor proveedor = buscarPorIdOrThrow(id);
        proveedorMapper.updateEntity(proveedor, input);
        vincularPersona(proveedor, input);
        proveedor = actualizar(proveedor);
        return proveedorMapper.toOutput(proveedor);
    }

    private void vincularPersona(Proveedor proveedor, ProveedorInput input) {
        boolean fisica = "FISICA".equalsIgnoreCase(input.tipoPersona());
        String documento = input.documento() != null && !input.documento().isBlank()
                ? input.documento()
                : input.ruc();
        Persona persona = personaService.reutilizarOCrear(new PersonaInput(
                fisica ? input.nombre() : input.razonSocial(),
                fisica ? input.apellido() : null,
                documento,
                input.email(),
                input.telefono(),
                input.direccion(),
                null
        ));
        proveedor.setPersona(persona);
        proveedor.setDocumento(persona.getDocumento());
    }

    /**
     * Lista todos los proveedores como output DTOs.
     */
    @Transactional(readOnly = true)
    public List<ProveedorOutput> listarTodosProveedores() {
        return proveedorRepository.findAll()
                .stream()
                .map(proveedorMapper::toOutput)
                .collect(Collectors.toList());
    }

    /**
     * Lista solo los proveedores activos ordenados.
     */
    @Transactional(readOnly = true)
    public List<ProveedorOutput> listarProveedoresActivos() {
        return proveedorRepository.findAllActivosOrdenados()
                .stream()
                .map(proveedorMapper::toOutput)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un proveedor por ID como output DTO.
     */
    @Transactional(readOnly = true)
    public ProveedorOutput obtenerProveedorPorId(Long id) {
        Proveedor proveedor = buscarPorIdOrThrow(id);
        return proveedorMapper.toOutput(proveedor);
    }

    /**
     * Busca un proveedor por RUC, con o sin dígito verificador.
     * Devuelve null si todavía no está registrado.
     */
    @Transactional(readOnly = true)
    public ProveedorOutput buscarPorRuc(String ruc) {
        return buscarEntidadPorRuc(ruc).map(proveedorMapper::toOutput).orElse(null);
    }

    private Optional<Proveedor> buscarEntidadPorRuc(String ruc) {
        if (ruc == null || ruc.isBlank()) {
            return Optional.empty();
        }
        String normalizado = ruc.trim().replaceAll("[\\s.]", "").toUpperCase();
        String base = normalizado.replaceFirst("-\\d$", "");
        return proveedorRepository
                .buscarPorRucFlexible(List.of(normalizado, base), base + "-_")
                .stream()
                .findFirst();
    }
}
