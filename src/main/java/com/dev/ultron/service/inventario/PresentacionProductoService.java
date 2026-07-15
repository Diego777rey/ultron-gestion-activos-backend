package com.dev.ultron.service.inventario;

import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.dto.inventario.input.PresentacionProductoInput;
import com.dev.ultron.dto.inventario.output.PresentacionProductoOutput;
import com.dev.ultron.dto.inventario.mapper.PresentacionProductoMapper;
import com.dev.ultron.repository.inventario.PresentacionProductoRepository;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresentacionProductoService extends GenericCrudService<PresentacionProducto, Long> {

    private final PresentacionProductoRepository repository;
    private final PresentacionProductoMapper mapper;
    private final ProductoRepository productoRepository;

    public PresentacionProductoService(PresentacionProductoRepository repository, PresentacionProductoMapper mapper, ProductoRepository productoRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.productoRepository = productoRepository;
    }

    @Override
    protected JpaRepository<PresentacionProducto, Long> getRepository() {
        return repository;
    }

    @Transactional
    public PresentacionProductoOutput save(PresentacionProductoInput input) {
        Producto producto = resolverProducto(input.getIdProducto());
        validarCodigoBarras(input.getCodigoBarras(), null);
        PresentacionProducto entidad = mapper.toEntity(input, producto);
        entidad = guardar(entidad);
        aplicarPrincipalUnico(producto.getId_producto(), entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional
    public PresentacionProductoOutput update(Long id, PresentacionProductoInput input) {
        PresentacionProducto entidad = buscarPorIdOrThrow(id);
        Producto producto = entidad.getProducto();
        if (input.getIdProducto() != null) {
            producto = resolverProducto(input.getIdProducto());
        }
        validarCodigoBarras(input.getCodigoBarras(), id);
        mapper.updateEntity(entidad, input, producto);
        entidad = actualizar(entidad);
        aplicarPrincipalUnico(producto.getId_producto(), entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional(readOnly = true)
    public List<PresentacionProductoOutput> findByProducto(Long idProducto) {
        return repository.findByProductoOrdenado(idProducto)
                .stream()
                .map(mapper::toOutput)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PresentacionProductoOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }

    private Producto resolverProducto(Long idProducto) {
        if (idProducto == null) {
            throw new EntityNotFoundException("Debe indicar el producto de la presentacion");
        }
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + idProducto));
    }

    /**
     * Regla de negocio: el codigo de barras es unico entre todas las presentaciones.
     */
    private void validarCodigoBarras(String codigoBarras, Long idActual) {
        if (codigoBarras == null || codigoBarras.trim().isEmpty()) {
            return;
        }
        String normalizado = codigoBarras.trim().toUpperCase();
        repository.findAll().stream()
                .filter(p -> p.getCodigoBarras() != null && p.getCodigoBarras().equalsIgnoreCase(normalizado))
                .filter(p -> idActual == null || !p.getId_presentacion_producto().equals(idActual))
                .findFirst()
                .ifPresent(p -> {
                    throw new IllegalArgumentException("El codigo de barras '" + normalizado + "' ya esta asignado a otra presentacion");
                });
    }

    /**
     * Regla de negocio: un producto solo puede tener una presentacion principal.
     */
    private void aplicarPrincipalUnico(Long idProducto, PresentacionProducto principal) {
        if (principal == null || !principal.isPrincipal()) {
            return;
        }
        List<PresentacionProducto> principales = repository.findPrincipalesDeProducto(idProducto);
        for (PresentacionProducto p : principales) {
            if (!p.getId_presentacion_producto().equals(principal.getId_presentacion_producto())) {
                p.setPrincipal(false);
                repository.save(p);
            }
        }
    }
}
