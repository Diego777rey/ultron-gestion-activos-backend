package com.dev.ultron.service.inventario;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.CategoriaProducto;
import com.dev.ultron.dto.inventario.input.ProductoInput;
import com.dev.ultron.dto.inventario.output.ProductoOutput;
import com.dev.ultron.dto.inventario.mapper.ProductoMapper;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.inventario.CategoriaProductoRepository;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.generic.SearchNormalizer;
import com.dev.ultron.generic.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService extends GenericCrudService<Producto, Long> {

    private final ProductoRepository repository;
    private final ProductoMapper mapper;
    private final CategoriaProductoRepository categoriaProductoRepository;

    public ProductoService(ProductoRepository repository, ProductoMapper mapper, CategoriaProductoRepository categoriaProductoRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.categoriaProductoRepository = categoriaProductoRepository;
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
        entidad = guardar(entidad);
        return mapper.toOutput(entidad);
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
        entidad = actualizar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional(readOnly = true)
    public List<ProductoOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoOutput> findAllPaginated(int page, int size, String filter) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Producto> pagina;
        if (filter != null && !filter.trim().isEmpty()) {
            pagina = repository.findAll(pageRequest);
        } else {
            pagina = listarPaginado(pageRequest);
        }
        return new PageResponse<>(pagina.map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public ProductoOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }
}
