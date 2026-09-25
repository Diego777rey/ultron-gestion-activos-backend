package com.dev.ultron.service.inventario;

import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.CategoriaProducto;
import com.dev.ultron.dto.inventario.input.PresentacionProductoInput;
import com.dev.ultron.dto.inventario.input.ProductoInput;
import com.dev.ultron.dto.inventario.output.PresentacionProductoOutput;
import com.dev.ultron.dto.inventario.output.ProductoOutput;
import com.dev.ultron.dto.inventario.mapper.ProductoMapper;
import com.dev.ultron.repository.inventario.PresentacionProductoRepository;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.inventario.CategoriaProductoRepository;
import com.dev.ultron.service.operaciones.StockProductoSectorService;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.generic.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductoService extends GenericCrudService<Producto, Long> {

    private final ProductoRepository repository;
    private final PresentacionProductoRepository presentacionProductoRepository;
    private final ProductoMapper mapper;
    private final CategoriaProductoRepository categoriaProductoRepository;
    private final StockProductoSectorService stockProductoSectorService;

    public ProductoService(
            ProductoRepository repository,
            PresentacionProductoRepository presentacionProductoRepository,
            ProductoMapper mapper,
            CategoriaProductoRepository categoriaProductoRepository,
            StockProductoSectorService stockProductoSectorService
    ) {
        this.repository = repository;
        this.presentacionProductoRepository = presentacionProductoRepository;
        this.mapper = mapper;
        this.categoriaProductoRepository = categoriaProductoRepository;
        this.stockProductoSectorService = stockProductoSectorService;
    }

    @Override
    protected JpaRepository<Producto, Long> getRepository() {
        return repository;
    }

    @Transactional
    public ProductoOutput save(ProductoInput input) {
        CategoriaProducto categoria = categoriaProductoRepository.findById(input.getIdCategoriaProducto())
                .orElseThrow(() -> new EntityNotFoundException("Categoria de Producto no encontrada con id: " + input.getIdCategoriaProducto()));
        Producto entidad = mapper.toEntity(input, categoria);
        if (entidad.getStock() == null) {
            entidad.setStock(BigDecimal.ZERO);
        }
        sincronizarPresentaciones(entidad, input.getPresentaciones());
        reflejarPresentacionPrincipal(entidad, input.getPresentaciones());
        validarCodigosBarras(entidad);
        entidad = guardar(entidad);
        stockProductoSectorService.asegurarStockInicial(entidad, entidad.getStock());
        return toOutput(entidad);
    }

    @Transactional
    public ProductoOutput update(Long id, ProductoInput input) {
        Producto entidad = buscarPorIdOrThrow(id);
        CategoriaProducto categoria = null;
        if (input.getIdCategoriaProducto() != null) {
            categoria = categoriaProductoRepository.findById(input.getIdCategoriaProducto())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria de Producto no encontrada con id: " + input.getIdCategoriaProducto()));
        } else {
            categoria = entidad.getCategoriaProducto();
        }
        mapper.updateEntity(entidad, input, categoria);
        sincronizarPresentaciones(entidad, input.getPresentaciones());
        reflejarPresentacionPrincipal(entidad, input.getPresentaciones());
        validarCodigosBarras(entidad);
        entidad = actualizar(entidad);
        stockProductoSectorService.resyncProductoStock(entidad.getId_producto());
        return toOutput(entidad);
    }

    @Transactional(readOnly = true)
    public List<ProductoOutput> findAll() {
        return listarTodos().stream().map(this::toOutput).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoOutput> findAllPaginated(int page, int size, String filter) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Producto> pagina;
        if (filter != null && !filter.trim().isEmpty()) {
            pagina = repository.buscar(filter.trim(), pageRequest);
        } else {
            pagina = listarPaginado(pageRequest);
        }
        return new PageResponse<>(pagina.map(this::toOutput));
    }

    @Transactional(readOnly = true)
    public ProductoOutput findById(Long id) {
        return toOutput(buscarPorIdOrThrow(id));
    }

    private ProductoOutput toOutput(Producto entidad) {
        ProductoOutput output = mapper.toOutput(entidad);
        List<PresentacionProducto> presentaciones = entidad.getPresentaciones();
        if (presentaciones == null || presentaciones.isEmpty()) {
            output.setPresentaciones(List.of());
            return output;
        }
        output.setPresentaciones(presentaciones.stream().map(this::toPresentacionOutput).toList());
        return output;
    }

    private PresentacionProductoOutput toPresentacionOutput(PresentacionProducto entidad) {
        PresentacionProductoOutput output = new PresentacionProductoOutput();
        output.setId_presentacion_producto(entidad.getId_presentacion_producto());
        output.setDescripcion(entidad.getDescripcion());
        output.setCodigoBarras(entidad.getCodigoBarras());
        output.setCantidad(entidad.getCantidad());
        output.setPrecio(entidad.getPrecio());
        return output;
    }

    /**
     * Reemplaza las presentaciones del producto cuando el cliente envía la lista.
     * {@code null} conserva las que ya existen, para no borrarlas en actualizaciones
     * que no incluyen el campo.
     */
    private void sincronizarPresentaciones(Producto producto, List<PresentacionProductoInput> inputs) {
        if (inputs == null) {
            return;
        }
        if (producto.getPresentaciones() == null) {
            producto.setPresentaciones(new ArrayList<>());
        }

        List<PresentacionProducto> actuales = producto.getPresentaciones();
        Map<Long, PresentacionProducto> porId = new HashMap<>();
        for (PresentacionProducto actual : actuales) {
            if (actual.getId_presentacion_producto() != null) {
                porId.put(actual.getId_presentacion_producto(), actual);
            }
        }

        Set<String> descripciones = new HashSet<>();
        List<PresentacionProducto> conservadas = new ArrayList<>();
        int orden = 0;
        for (PresentacionProductoInput input : inputs) {
            if (input == null) {
                continue;
            }
            String descripcion = normalizarDescripcion(input.getDescripcion());
            if (descripcion.isEmpty()) {
                throw new IllegalArgumentException("La descripción de la presentación es obligatoria");
            }
            if (descripcion.length() > 150) {
                throw new IllegalArgumentException("La descripción de la presentación no puede superar 150 caracteres");
            }
            if (!descripciones.add(descripcion)) {
                throw new IllegalArgumentException("La presentación \"" + descripcion + "\" está repetida");
            }
            if (input.getCantidad() == null || input.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad de \"" + descripcion + "\" debe ser mayor a cero");
            }
            if (input.getPrecio() == null || input.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio de \"" + descripcion + "\" no puede ser negativo");
            }
            String codigoBarras = normalizarCodigo(input.getCodigoBarras());
            if (codigoBarras.isEmpty()) {
                throw new IllegalArgumentException("El código de barras de \"" + descripcion + "\" es obligatorio");
            }
            if (codigoBarras.length() > 100) {
                throw new IllegalArgumentException("El código de barras de \"" + descripcion + "\" no puede superar 100 caracteres");
            }

            PresentacionProducto entidad = input.getId_presentacion_producto() == null
                    ? null
                    : porId.get(input.getId_presentacion_producto());
            if (entidad == null) {
                entidad = new PresentacionProducto();
                entidad.setProducto(producto);
            }
            entidad.setDescripcion(descripcion);
            entidad.setCodigoBarras(codigoBarras);
            entidad.setCantidad(input.getCantidad());
            entidad.setPrecio(input.getPrecio());
            entidad.setOrden(orden++);
            conservadas.add(entidad);
        }

        actuales.removeIf(actual -> conservadas.stream().noneMatch(conservada -> conservada == actual));
        for (PresentacionProducto entidad : conservadas) {
            boolean yaEsta = false;
            for (PresentacionProducto actual : actuales) {
                if (actual == entidad) {
                    yaEsta = true;
                    break;
                }
            }
            if (!yaEsta) {
                actuales.add(entidad);
            }
        }
    }

    /**
     * El precio y el código de barras del producto salen de la primera presentación.
     * Si el cliente envía la lista, tiene que haber al menos una.
     */
    private void reflejarPresentacionPrincipal(Producto producto, List<PresentacionProductoInput> inputs) {
        if (inputs == null) {
            return;
        }
        List<PresentacionProducto> presentaciones = producto.getPresentaciones();
        if (presentaciones == null || presentaciones.isEmpty()) {
            throw new IllegalArgumentException("El producto debe tener al menos una presentación");
        }
        PresentacionProducto principal = presentaciones.get(0);
        producto.setPrecioVenta(principal.getPrecio());
        producto.setCodigoBarras(principal.getCodigoBarras());
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            producto.setCodigo(principal.getCodigoBarras());
        }
    }

    private String normalizarDescripcion(String descripcion) {
        return descripcion == null ? "" : descripcion.trim().toUpperCase();
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim();
    }

    private void validarCodigosBarras(Producto producto) {
        List<PresentacionProducto> presentaciones = producto.getPresentaciones() == null
                ? List.of()
                : producto.getPresentaciones();
        String codigoProducto = normalizarCodigo(producto.getCodigoBarras());
        Set<String> vistos = new HashSet<>();
        List<String> ajenos = new ArrayList<>();
        for (PresentacionProducto presentacion : presentaciones) {
            String codigo = normalizarCodigo(presentacion.getCodigoBarras());
            if (codigo.isEmpty()) {
                throw new IllegalArgumentException("El código de barras de la presentación es obligatorio");
            }
            if (!vistos.add(codigo.toLowerCase())) {
                throw new IllegalArgumentException("El código de barras \"" + codigo + "\" está repetido");
            }
            if (!codigo.equalsIgnoreCase(codigoProducto)) {
                ajenos.add(codigo.toLowerCase());
            }
        }

        Long idProducto = producto.getId_producto();
        if (!codigoProducto.isEmpty()) {
            if (presentacionProductoRepository.codigoUsadoEnOtraPresentacion(codigoProducto, idProducto)) {
                throw new IllegalArgumentException("El código de barras del producto ya está usado en una presentación");
            }
            List<String> usadoEnOtroProducto = repository.codigosBarrasUsadosPorOtrosProductos(
                    List.of(codigoProducto.toLowerCase()), idProducto);
            if (!usadoEnOtroProducto.isEmpty()) {
                throw new IllegalArgumentException(
                        "El código de barras \"" + codigoProducto + "\" ya pertenece a otro producto");
            }
        }
        if (ajenos.isEmpty()) {
            return;
        }

        List<String> usadosEnProductos = repository.codigosBarrasUsadosPorOtrosProductos(ajenos, idProducto);
        if (!usadosEnProductos.isEmpty()) {
            throw new IllegalArgumentException(
                    "El código de barras \"" + usadosEnProductos.get(0) + "\" ya pertenece a otro producto");
        }
        List<String> usadosEnPresentaciones = presentacionProductoRepository.codigosUsadosEnOtrasPresentaciones(ajenos, idProducto);
        if (!usadosEnPresentaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "El código de barras \"" + usadosEnPresentaciones.get(0) + "\" ya está usado en otra presentación");
        }
    }
}
