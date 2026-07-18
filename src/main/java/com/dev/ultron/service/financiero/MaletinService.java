package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Maletin;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.financiero.input.MaletinInput;
import com.dev.ultron.dto.financiero.mapper.MaletinMapper;
import com.dev.ultron.dto.financiero.output.MaletinOutput;
import com.dev.ultron.dto.personas.mapper.PersonaMapper;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.MaletinRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.personas.PersonaRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaletinService extends GenericCrudService<Maletin, Long> {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final MaletinRepository repository;
    private final MaletinMapper mapper;
    private final PersonaMapper personaMapper;
    private final PersonaRepository personaRepository;
    private final SectorRepository sectorRepository;
    private final SesionCajaRepository sesionCajaRepository;

    public MaletinService(
            MaletinRepository repository,
            MaletinMapper mapper,
            PersonaMapper personaMapper,
            PersonaRepository personaRepository,
            SectorRepository sectorRepository,
            SesionCajaRepository sesionCajaRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.personaMapper = personaMapper;
        this.personaRepository = personaRepository;
        this.sectorRepository = sectorRepository;
        this.sesionCajaRepository = sesionCajaRepository;
    }

    @Override
    protected JpaRepository<Maletin, Long> getRepository() {
        return repository;
    }

    @Transactional
    public MaletinOutput save(MaletinInput input) {
        Persona responsable = resolveResponsable(input.getIdResponsable());
        Sector sector = resolveSector(input.getIdSector());
        Maletin entidad = mapper.toEntity(input, responsable, sector);
        entidad.setAbierto(false);
        if (entidad.getActivo() == null) {
            entidad.setActivo(true);
        }
        return toEnrichedOutput(guardar(entidad));
    }

    @Transactional
    public MaletinOutput update(Long id, MaletinInput input) {
        Maletin entidad = buscarPorIdOrThrow(id);
        Persona responsable = input.getIdResponsable() != null
                ? resolveResponsable(input.getIdResponsable())
                : entidad.getResponsable();
        Sector sector = input.getIdSector() != null
                ? resolveSector(input.getIdSector())
                : entidad.getSector();
        mapper.updateEntity(entidad, input, responsable, sector);
        return toEnrichedOutput(actualizar(entidad));
    }

    @Transactional(readOnly = true)
    public List<MaletinOutput> findAll() {
        return listarTodos().stream().map(this::toEnrichedOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaletinOutput> findDisponibles(Long idSector) {
        return repository.findDisponibles(idSector).stream()
                .map(this::toEnrichedOutput)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<MaletinOutput> findAllPaginated(int page, int size, String filter) {
        return new PageResponse<>(
                repository.buscar(filter, PageRequest.of(page, size)).map(this::toEnrichedOutput)
        );
    }

    @Transactional(readOnly = true)
    public MaletinOutput findById(Long id) {
        return toEnrichedOutput(buscarPorIdOrThrow(id));
    }

    private MaletinOutput toEnrichedOutput(Maletin entity) {
        MaletinOutput output = mapper.toOutput(entity);

        sesionCajaRepository.findAbiertaPorMaletin(entity.getId_maletin())
                .ifPresent(sesion -> output.setIdCajaActual(sesion.getId_sesion_caja()));

        SesionCaja ultima = sesionCajaRepository
                .findUltimaPorMaletin(entity.getId_maletin(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);

        if (ultima != null) {
            if (ultima.getFechaCierre() != null) {
                output.setUltimoMovimiento(ultima.getFechaCierre().format(FECHA_FMT));
            } else if (ultima.getFechaApertura() != null) {
                output.setUltimoMovimiento(ultima.getFechaApertura().format(FECHA_FMT));
            }
            if (ultima.getPersona() != null) {
                output.setUltimoResponsable(personaMapper.toOutput(ultima.getPersona()));
            }
        } else if (entity.getResponsable() != null) {
            output.setUltimoResponsable(personaMapper.toOutput(entity.getResponsable()));
        }

        return output;
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
            throw new EntityNotFoundException("El sector es obligatorio para el maletín");
        }
        return sectorRepository.findById(idSector)
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado con id: " + idSector));
    }
}
