package com.dev.ultron.service.sectores;

import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.domain.sectores.Zona;
import com.dev.ultron.dto.sectores.input.ZonaInput;
import com.dev.ultron.dto.sectores.mapper.ZonaMapper;
import com.dev.ultron.dto.sectores.output.ZonaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
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
public class ZonaService extends GenericCrudService<Zona, Long> {

    private final ZonaRepository repository;
    private final ZonaMapper mapper;
    private final SectorRepository sectorRepository;

    public ZonaService(ZonaRepository repository, ZonaMapper mapper, SectorRepository sectorRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.sectorRepository = sectorRepository;
    }

    @Override
    protected JpaRepository<Zona, Long> getRepository() {
        return repository;
    }

    @Transactional
    public ZonaOutput save(ZonaInput input) {
        Sector sector = sectorRepository.findById(input.getIdSector())
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado con id: " + input.getIdSector()));
        Zona entidad = mapper.toEntity(input, sector);
        if (input.getEstado() == null) {
            entidad.setEstado(true);
        }
        entidad = guardar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional
    public ZonaOutput update(Long id, ZonaInput input) {
        Zona entidad = buscarPorIdOrThrow(id);
        Sector sector;
        if (input.getIdSector() != null) {
            sector = sectorRepository.findById(input.getIdSector())
                    .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado con id: " + input.getIdSector()));
        } else {
            sector = entidad.getSector();
        }
        mapper.updateEntity(entidad, input, sector);
        entidad = actualizar(entidad);
        return mapper.toOutput(entidad);
    }

    @Transactional(readOnly = true)
    public List<ZonaOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<ZonaOutput> findAllPaginated(int page, int size, String filter) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Zona> pagina = listarPaginado(pageRequest);
        return new PageResponse<>(pagina.map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public List<ZonaOutput> findBySector(Long idSector) {
        if (!sectorRepository.existsById(idSector)) {
            throw new EntityNotFoundException("Sector no encontrado con id: " + idSector);
        }
        return repository.findBySectorId(idSector).stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ZonaOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }
}
