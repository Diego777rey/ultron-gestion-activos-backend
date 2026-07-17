package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Maletin;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.financiero.input.MaletinInput;
import com.dev.ultron.dto.financiero.mapper.MaletinMapper;
import com.dev.ultron.dto.financiero.output.MaletinOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.MaletinRepository;
import com.dev.ultron.repository.personas.PersonaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaletinService extends GenericCrudService<Maletin, Long> {

    private final MaletinRepository repository;
    private final MaletinMapper mapper;
    private final PersonaRepository personaRepository;

    public MaletinService(MaletinRepository repository, MaletinMapper mapper, PersonaRepository personaRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.personaRepository = personaRepository;
    }

    @Override
    protected JpaRepository<Maletin, Long> getRepository() {
        return repository;
    }

    @Transactional
    public MaletinOutput save(MaletinInput input) {
        Persona responsable = resolveResponsable(input.getIdResponsable());
        Maletin entidad = mapper.toEntity(input, responsable);
        entidad.setEstado("CERRADO");
        if (entidad.getBalancePyg() == null) {
            entidad.setBalancePyg(BigDecimal.ZERO);
        }
        if (entidad.getBalanceUsd() == null) {
            entidad.setBalanceUsd(BigDecimal.ZERO);
        }
        if (entidad.getBalanceBrl() == null) {
            entidad.setBalanceBrl(BigDecimal.ZERO);
        }
        if (entidad.getActivo() == null) {
            entidad.setActivo(true);
        }
        return mapper.toOutput(guardar(entidad));
    }

    @Transactional
    public MaletinOutput update(Long id, MaletinInput input) {
        Maletin entidad = buscarPorIdOrThrow(id);
        Persona responsable = input.getIdResponsable() != null
                ? resolveResponsable(input.getIdResponsable())
                : entidad.getResponsable();
        mapper.updateEntity(entidad, input, responsable);
        return mapper.toOutput(actualizar(entidad));
    }

    @Transactional(readOnly = true)
    public List<MaletinOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaletinOutput> findDisponibles() {
        return repository.findByActivoTrueAndEstado("CERRADO").stream()
                .map(mapper::toOutput)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<MaletinOutput> findAllPaginated(int page, int size, String filter) {
        return new PageResponse<>(repository.buscar(filter, PageRequest.of(page, size)).map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public MaletinOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }

    private Persona resolveResponsable(Long idResponsable) {
        if (idResponsable == null) {
            return null;
        }
        return personaRepository.findById(idResponsable)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + idResponsable));
    }
}
