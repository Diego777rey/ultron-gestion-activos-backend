package com.dev.ultron.service.compras;

import com.dev.ultron.domain.compras.Proveedor;
import com.dev.ultron.domain.compras.Vendedor;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.compras.input.VendedorInput;
import com.dev.ultron.dto.compras.mapper.VendedorMapper;
import com.dev.ultron.dto.compras.output.VendedorOutput;
import com.dev.ultron.dto.personas.input.PersonaInput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.repository.compras.ProveedorRepository;
import com.dev.ultron.repository.compras.VendedorRepository;
import com.dev.ultron.service.personas.PersonaService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Vendedor para gestión de vendedores de proveedores.
 */
@Service
public class VendedorService extends GenericCrudService<Vendedor, Long> {

    private final VendedorRepository vendedorRepository;
    private final ProveedorRepository proveedorRepository;
    private final VendedorMapper vendedorMapper;
    private final PersonaService personaService;

    public VendedorService(VendedorRepository vendedorRepository,
                          ProveedorRepository proveedorRepository,
                          VendedorMapper vendedorMapper,
                          PersonaService personaService) {
        this.vendedorRepository = vendedorRepository;
        this.proveedorRepository = proveedorRepository;
        this.vendedorMapper = vendedorMapper;
        this.personaService = personaService;
    }

    @Override
    protected JpaRepository<Vendedor, Long> getRepository() {
        return vendedorRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Vendedor vendedor) {
        if (vendedor.getNombre() == null || vendedor.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (vendedor.getApellido() == null || vendedor.getApellido().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (vendedor.getProveedor() == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
        if ((vendedor.getTelefono() == null || vendedor.getTelefono().isEmpty()) &&
            (vendedor.getCelular() == null || vendedor.getCelular().isEmpty())) {
            throw new IllegalArgumentException("Debe proporcionar al menos un teléfono o celular");
        }
    }

    /**
     * Registra un nuevo vendedor.
     */
    @Transactional
    public VendedorOutput registrarVendedor(VendedorInput input) {
        Vendedor vendedor = vendedorMapper.toEntity(input);
        
        Proveedor proveedor = proveedorRepository.findById(input.idProveedor())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + input.idProveedor()));
        
        vendedor.setProveedor(proveedor);
        vincularPersona(vendedor, input);
        vendedor = guardar(vendedor);
        return vendedorMapper.toOutput(vendedor);
    }

    /**
     * Actualiza un vendedor existente.
     */
    @Transactional
    public VendedorOutput actualizarVendedor(Long id, VendedorInput input) {
        Vendedor vendedor = buscarPorIdOrThrow(id);
        
        if (input.idProveedor() != null && !input.idProveedor().equals(vendedor.getProveedor().getId_proveedor())) {
            Proveedor proveedor = proveedorRepository.findById(input.idProveedor())
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + input.idProveedor()));
            vendedor.setProveedor(proveedor);
        }
        
        vendedorMapper.updateEntity(vendedor, input);
        vincularPersona(vendedor, input);
        vendedor = actualizar(vendedor);
        return vendedorMapper.toOutput(vendedor);
    }

    private void vincularPersona(Vendedor vendedor, VendedorInput input) {
        if (input.documento() == null || input.documento().isBlank()) {
            return;
        }
        Persona persona = personaService.reutilizarOCrear(new PersonaInput(
                input.nombre(),
                input.apellido(),
                input.documento(),
                input.email(),
                input.telefono(),
                null,
                null
        ));
        vendedor.setPersona(persona);
        vendedor.setDocumento(persona.getDocumento());
    }

    /**
     * Lista todos los vendedores como output DTOs.
     */
    @Transactional(readOnly = true)
    public List<VendedorOutput> listarTodosVendedores() {
        return vendedorRepository.findAll()
                .stream()
                .map(vendedorMapper::toOutput)
                .collect(Collectors.toList());
    }

    /**
     * Lista solo los vendedores activos.
     */
    @Transactional(readOnly = true)
    public List<VendedorOutput> listarVendedoresActivos() {
        return vendedorRepository.findByActivoTrue()
                .stream()
                .map(vendedorMapper::toOutput)
                .collect(Collectors.toList());
    }

    /**
     * Lista vendedores por proveedor.
     */
    @Transactional(readOnly = true)
    public List<VendedorOutput> listarVendedoresPorProveedor(Long idProveedor) {
        return vendedorRepository.findByProveedorActivosOrdenados(idProveedor)
                .stream()
                .map(vendedorMapper::toOutput)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un vendedor por ID como output DTO.
     */
    @Transactional(readOnly = true)
    public VendedorOutput obtenerVendedorPorId(Long id) {
        Vendedor vendedor = buscarPorIdOrThrow(id);
        return vendedorMapper.toOutput(vendedor);
    }
}
