package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.financiero.input.CajaInput;
import com.dev.ultron.dto.financiero.mapper.CajaMapper;
import com.dev.ultron.dto.financiero.output.CajaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.personas.PersonaRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CajaService extends GenericCrudService<Caja, Long> {

    private final CajaRepository repository;
    private final CajaMapper mapper;
    private final PersonaRepository personaRepository;
    private final SectorRepository sectorRepository;

    public CajaService(
            CajaRepository repository,
            CajaMapper mapper,
            PersonaRepository personaRepository,
            SectorRepository sectorRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.personaRepository = personaRepository;
        this.sectorRepository = sectorRepository;
    }

    @Override
    protected JpaRepository<Caja, Long> getRepository() {
        return repository;
    }

    @Transactional
    public CajaOutput save(CajaInput input) {
        Persona responsable = resolveResponsable(input.getIdResponsable());
        Sector sector = resolveSector(input.getIdSector());
        Caja entidad = mapper.toEntity(input, responsable, sector);
        if (entidad.getSaldo_actual() == null) {
            entidad.setSaldo_actual(BigDecimal.ZERO);
        }
        if (entidad.getActiva() == null) {
            entidad.setActiva(true);
        }
        return mapper.toOutput(guardar(entidad));
    }

    @Transactional
    public CajaOutput update(Long id, CajaInput input) {
        Caja entidad = buscarPorIdOrThrow(id);
        Persona responsable = input.getIdResponsable() != null
                ? resolveResponsable(input.getIdResponsable())
                : entidad.getResponsable();
        Sector sector = input.getIdSector() != null
                ? resolveSector(input.getIdSector())
                : entidad.getSector();
        mapper.updateEntity(entidad, input, responsable, sector);
        return mapper.toOutput(actualizar(entidad));
    }

    @Transactional(readOnly = true)
    public List<CajaOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<CajaOutput> findAllPaginated(int page, int size, String filter) {
        return new PageResponse<>(repository.buscar(filter, PageRequest.of(page, size)).map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public CajaOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }

    private Persona resolveResponsable(Long idResponsable) {
        if (idResponsable == null) {
            return null;
        }
        return personaRepository.findById(idResponsable)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + idResponsable));
    }

    private Sector resolveSector(Long idSector) {
        if (idSector == null) {
            throw new EntityNotFoundException("El sector es obligatorio para la caja");
        }
        return sectorRepository.findById(idSector)
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado con id: " + idSector));
    }
}
