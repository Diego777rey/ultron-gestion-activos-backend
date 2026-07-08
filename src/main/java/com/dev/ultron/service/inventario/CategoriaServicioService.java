package com.dev.ultron.service.inventario;

import com.dev.ultron.domain.inventario.CategoriaServicio;
import com.dev.ultron.dto.inventario.input.CategoriaServicioInput;
import com.dev.ultron.dto.inventario.output.CategoriaServicioOutput;
import com.dev.ultron.dto.inventario.mapper.CategoriaServicioMapper;
import com.dev.ultron.repository.inventario.CategoriaServicioRepository;
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
public class CategoriaServicioService extends GenericCrudService<CategoriaServicio, Long> {

    private final CategoriaServicioRepository repository;
    private final CategoriaServicioMapper mapper;

    public CategoriaServicioService(CategoriaServicioRepository repository, CategoriaServicioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    protected JpaRepository<CategoriaServicio, Long> getRepository() {
        return repository;
    }

    @Transactional
    public CategoriaServicioOutput save(CategoriaServicioInput input) {
        CategoriaServicio padre = null;
        if (input.getIdCategoriaPadre() != null) {
            padre = repository.findById(input.getIdCategoriaPadre())
                    .orElseThrow(() -> new EntityNotFoundException("CategoriaPadre no encontrada con id: " + input.getIdCategoriaPadre()));
        }
        CategoriaServicio entidad = mapper.toEntity(input, padre);
        entidad = guardar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional
    public CategoriaServicioOutput update(Long id, CategoriaServicioInput input) {
        CategoriaServicio entidad = buscarPorIdOrThrow(id);
        CategoriaServicio padre = null;
        if (input.getIdCategoriaPadre() != null) {
            padre = repository.findById(input.getIdCategoriaPadre())
                    .orElseThrow(() -> new EntityNotFoundException("CategoriaPadre no encontrada con id: " + input.getIdCategoriaPadre()));
        }
        mapper.updateEntity(entidad, input, padre);
        entidad = actualizar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional(readOnly = true)
    public List<CategoriaServicioOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoriaServicioOutput> findAllPaginated(int page, int size, String filter) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<CategoriaServicio> pagina;
        if (filter != null && !filter.trim().isEmpty()) {
            pagina = repository.findAll(pageRequest);
        } else {
            pagina = listarPaginado(pageRequest);
        }
        return new PageResponse<>(pagina.map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public CategoriaServicioOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }
}
