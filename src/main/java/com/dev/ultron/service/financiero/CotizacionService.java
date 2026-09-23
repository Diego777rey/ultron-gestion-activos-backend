package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Cotizacion;
import com.dev.ultron.dto.financiero.input.CotizacionInput;
import com.dev.ultron.dto.financiero.mapper.CotizacionMapper;
import com.dev.ultron.dto.financiero.output.CotizacionOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.CotizacionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CotizacionService extends GenericCrudService<Cotizacion, Long> {

    private final CotizacionRepository repository;
    private final CotizacionMapper mapper;

    public CotizacionService(
            CotizacionRepository repository,
            CotizacionMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    protected JpaRepository<Cotizacion, Long> getRepository() {
        return repository;
    }

    @Transactional
    public CotizacionOutput save(CotizacionInput input) {
        Cotizacion entidad = mapper.toEntity(input);
        entidad.setFechaActualizacion(LocalDateTime.now());
        if (entidad.getActiva() == null) {
            entidad.setActiva(true);
        }
        return mapper.toOutput(guardar(entidad));
    }

    @Transactional
    public CotizacionOutput update(Long id, CotizacionInput input) {
        Cotizacion entidad = buscarPorIdOrThrow(id);
        mapper.updateEntity(entidad, input);
        entidad.setFechaActualizacion(LocalDateTime.now());
        return mapper.toOutput(actualizar(entidad));
    }

    @Transactional(readOnly = true)
    public List<CotizacionOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<CotizacionOutput> findAllPaginated(int page, int size, String filter) {
        return new PageResponse<>(repository.buscar(filter, PageRequest.of(page, size)).map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public CotizacionOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }
}
