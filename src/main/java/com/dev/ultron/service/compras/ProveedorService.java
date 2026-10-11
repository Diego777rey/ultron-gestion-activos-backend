package com.dev.ultron.service.compras;

import com.dev.ultron.domain.compras.Proveedor;
import com.dev.ultron.dto.compras.input.ProveedorInput;
import com.dev.ultron.dto.compras.mapper.ProveedorMapper;
import com.dev.ultron.dto.compras.output.ProveedorOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.repository.compras.ProveedorRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Proveedor para gestión de proveedores del sistema.
 */
@Service
public class ProveedorService extends GenericCrudService<Proveedor, Long> {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;

    public ProveedorService(ProveedorRepository proveedorRepository, ProveedorMapper proveedorMapper) {
        this.proveedorRepository = proveedorRepository;
        this.proveedorMapper = proveedorMapper;
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
            if (proveedor.getNombre() == null || proveedor.getNombre().isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio para persona física");
            }
            if (proveedor.getApellido() == null || proveedor.getApellido().isEmpty()) {
                throw new IllegalArgumentException("El apellido es obligatorio para persona física");
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
        proveedor = guardar(proveedor);
        return proveedorMapper.toOutput(proveedor);
    }

    /**
     * Actualiza un proveedor existente.
     */
    @Transactional
    public ProveedorOutput actualizarProveedor(Long id, ProveedorInput input) {
        Proveedor proveedor = buscarPorIdOrThrow(id);
        proveedorMapper.updateEntity(proveedor, input);
        proveedor = actualizar(proveedor);
        return proveedorMapper.toOutput(proveedor);
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
     * Busca un proveedor por RUC.
     */
    @Transactional(readOnly = true)
    public ProveedorOutput buscarPorRuc(String ruc) {
        Proveedor proveedor = proveedorRepository.findByRuc(ruc)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró proveedor con RUC: " + ruc));
        return proveedorMapper.toOutput(proveedor);
    }
}
