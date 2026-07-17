package com.dev.ultron.service.sectores;

import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.sectores.input.SectorInput;
import com.dev.ultron.dto.sectores.mapper.SectorMapper;
import com.dev.ultron.dto.sectores.output.SectorOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.sectores.SectorRepository;
import com.dev.ultron.repository.sectores.ZonaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectorService extends GenericCrudService<Sector, Long> {

    private final SectorRepository repository;
    private final SectorMapper mapper;
    private final ZonaRepository zonaRepository;

    public SectorService(SectorRepository repository, SectorMapper mapper, ZonaRepository zonaRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.zonaRepository = zonaRepository;
    }

    @Override
    protected JpaRepository<Sector, Long> getRepository() {
        return repository;
    }

    @Override
    protected void validarAntesDeEliminar(Long id) {
        if (zonaRepository.existsBySectorId(id)) {
            throw new IllegalArgumentException("No se puede eliminar el sector porque tiene zonas asociadas");
        }
    }

    @Transactional
    public SectorOutput save(SectorInput input) {
        Sector entidad = mapper.toEntity(input);
        if (input.getEstado() == null) {
            entidad.setEstado(true);
        }
        entidad = guardar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional
    public SectorOutput update(Long id, SectorInput input) {
        Sector entidad = buscarPorIdOrThrow(id);
        mapper.updateEntity(entidad, input);
        entidad = actualizar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional(readOnly = true)
    public List<SectorOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<SectorOutput> findAllPaginated(int page, int size, String filter) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Sector> pagina = listarPaginado(pageRequest);
        return new PageResponse<>(pagina.map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public SectorOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }
}
