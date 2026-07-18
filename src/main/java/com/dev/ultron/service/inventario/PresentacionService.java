package com.dev.ultron.service.inventario;

import com.dev.ultron.domain.inventario.Presentacion;
import com.dev.ultron.dto.inventario.input.PresentacionInput;
import com.dev.ultron.dto.inventario.mapper.PresentacionMapper;
import com.dev.ultron.dto.inventario.output.PresentacionOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.inventario.PresentacionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresentacionService extends GenericCrudService<Presentacion, Long> {

    private final PresentacionRepository repository;
    private final PresentacionMapper mapper;

    public PresentacionService(PresentacionRepository repository, PresentacionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    protected JpaRepository<Presentacion, Long> getRepository() {
        return repository;
    }

    @Transactional
    public PresentacionOutput save(PresentacionInput input) {
        Presentacion entidad = mapper.toEntity(input);
        if (input.getEstado() == null) {
            entidad.setEstado(true);
        }
        if (entidad.getCantidad() == null) {
            entidad.setCantidad(BigDecimal.ONE);
        }
        entidad = guardar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional
    public PresentacionOutput update(Long id, PresentacionInput input) {
        Presentacion entidad = buscarPorIdOrThrow(id);
        mapper.updateEntity(entidad, input);
        entidad = actualizar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional(readOnly = true)
    public List<PresentacionOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<PresentacionOutput> findAllPaginated(int page, int size, String filter) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Presentacion> pagina = listarPaginado(pageRequest);
        return new PageResponse<>(pagina.map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public PresentacionOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }
}
