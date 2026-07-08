package com.dev.ultron.service.inventario;

import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.inventario.CategoriaServicio;
import com.dev.ultron.dto.inventario.input.ServicioInput;
import com.dev.ultron.dto.inventario.output.ServicioOutput;
import com.dev.ultron.dto.inventario.mapper.ServicioMapper;
import com.dev.ultron.repository.inventario.ServicioRepository;
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
public class ServicioService extends GenericCrudService<Servicio, Long> {

    private final ServicioRepository repository;
    private final ServicioMapper mapper;
    private final CategoriaServicioRepository categoriaServicioRepository;

    public ServicioService(ServicioRepository repository, ServicioMapper mapper, CategoriaServicioRepository categoriaServicioRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.categoriaServicioRepository = categoriaServicioRepository;
    }

    @Override
    protected JpaRepository<Servicio, Long> getRepository() {
        return repository;
    }

    @Transactional
    public ServicioOutput save(ServicioInput input) {
        CategoriaServicio categoria = categoriaServicioRepository.findById(input.getIdCategoriaServicio())
                .orElseThrow(() -> new EntityNotFoundException("Categoria de Servicio no encontrada con id: " + input.getIdCategoriaServicio()));
        Servicio entidad = mapper.toEntity(input, categoria);
        entidad = guardar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional
    public ServicioOutput update(Long id, ServicioInput input) {
        Servicio entidad = buscarPorIdOrThrow(id);
        CategoriaServicio categoria = null;
        if (input.getIdCategoriaServicio() != null) {
            categoria = categoriaServicioRepository.findById(input.getIdCategoriaServicio())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria de Servicio no encontrada con id: " + input.getIdCategoriaServicio()));
        } else {
            categoria = entidad.getCategoriaServicio();
        }
        mapper.updateEntity(entidad, input, categoria);
        entidad = actualizar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional(readOnly = true)
    public List<ServicioOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<ServicioOutput> findAllPaginated(int page, int size, String filter) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Servicio> pagina;
        if (filter != null && !filter.trim().isEmpty()) {
            pagina = repository.findAll(pageRequest);
        } else {
            pagina = listarPaginado(pageRequest);
        }
        return new PageResponse<>(pagina.map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public ServicioOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }
}
