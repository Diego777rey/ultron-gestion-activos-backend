package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.Terminal;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.financiero.input.CajaInput;
import com.dev.ultron.dto.financiero.mapper.CajaMapper;
import com.dev.ultron.dto.financiero.output.CajaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.TerminalRepository;
import com.dev.ultron.repository.personas.PersonaRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import com.dev.ultron.util.MacAddressUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CajaService extends GenericCrudService<Caja, Long> {

    private final CajaRepository repository;
    private final CajaMapper mapper;
    private final PersonaRepository personaRepository;
    private final SectorRepository sectorRepository;
    private final TerminalRepository terminalRepository;

    public CajaService(
            CajaRepository repository,
            CajaMapper mapper,
            PersonaRepository personaRepository,
            SectorRepository sectorRepository,
            TerminalRepository terminalRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.personaRepository = personaRepository;
        this.sectorRepository = sectorRepository;
        this.terminalRepository = terminalRepository;
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
        String macAnterior = null;
        aplicarVinculoTerminal(entidad, input.getMacAddress(), input.getNombreTerminal(), null);
        try {
            Caja guardada = guardar(entidad);
            sincronizarTerminalConCaja(guardada, macAnterior);
            return mapper.toOutput(guardada);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(mensajeMacDuplicada(ex), ex);
        }
    }

    @Transactional
    public CajaOutput update(Long id, CajaInput input) {
        Caja entidad = buscarPorIdOrThrow(id);
        String macAnterior = MacAddressUtil.safeNormalize(entidad.getMac_address());
        Persona responsable = input.getIdResponsable() != null
                ? resolveResponsable(input.getIdResponsable())
                : entidad.getResponsable();
        Sector sector = input.getIdSector() != null
                ? resolveSector(input.getIdSector())
                : entidad.getSector();
        mapper.updateEntity(entidad, input, responsable, sector);
        aplicarVinculoTerminal(entidad, input.getMacAddress(), input.getNombreTerminal(), id);
        try {
            Caja actualizada = actualizar(entidad);
            sincronizarTerminalConCaja(actualizada, macAnterior);
            return mapper.toOutput(actualizada);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(mensajeMacDuplicada(ex), ex);
        }
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

    @Transactional(readOnly = true)
    public List<CajaOutput> findDisponibles() {
        return findDisponibles(null);
    }

    /**
     * Cajas libres para abrir. Con {@code macAddress}:
     * <ul>
     *   <li>Si esa PC ya tiene caja vinculada → solo esa (si está libre).</li>
     *   <li>Si no → solo cajas sin vínculo de terminal.</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public List<CajaOutput> findDisponibles(String macAddress) {
        String mac = MacAddressUtil.safeNormalize(macAddress);
        if (mac == null) {
            return repository.findDisponibles().stream().map(mapper::toOutput).collect(Collectors.toList());
        }

        Optional<Caja> vinculada = repository.findByMacAddress(mac);
        if (vinculada.isPresent()) {
            Caja caja = vinculada.get();
            if (Boolean.FALSE.equals(caja.getActiva())) {
                return List.of();
            }
            Long id = caja.getId_caja();
            return repository.findDisponibles().stream()
                    .filter(c -> c.getId_caja().equals(id))
                    .map(mapper::toOutput)
                    .collect(Collectors.toList());
        }

        return repository.findDisponiblesSinVinculo().stream()
                .map(mapper::toOutput)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CajaOutput findByMac(String macAddress) {
        String mac = MacAddressUtil.normalize(macAddress);
        return repository.findByMacAddress(mac)
                .map(mapper::toOutput)
                .orElse(null);
    }

    /**
     * Reglas de vínculo caja ↔ terminal al abrir:
     * <ul>
     *   <li>Caja con MAC: solo esa PC puede abrirla.</li>
     *   <li>PC con caja asignada: solo puede abrir esa caja.</li>
     * </ul>
     */
    public void validarAperturaEnTerminal(Caja caja, String macAddressRaw) {
        String macPc = MacAddressUtil.safeNormalize(macAddressRaw);
        String macCaja = MacAddressUtil.safeNormalize(caja.getMac_address());

        if (macPc != null) {
            repository.findByMacAddress(macPc).ifPresent(cajaDeEstaPc -> {
                if (!cajaDeEstaPc.getId_caja().equals(caja.getId_caja())) {
                    String nombre = cajaDeEstaPc.getNombre() != null
                            ? cajaDeEstaPc.getNombre()
                            : ("#" + cajaDeEstaPc.getId_caja());
                    throw new IllegalArgumentException(
                            "Esta terminal solo puede abrir la caja " + nombre
                                    + ". Para mudar la caja, desvinculá o reasigná la MAC desde administración.");
                }
            });
        }

        if (macCaja != null) {
            if (macPc == null) {
                throw new IllegalArgumentException(
                        "La caja " + nombreCaja(caja)
                                + " está vinculada a una terminal. Abrila desde la aplicación de escritorio en esa PC.");
            }
            if (!macCaja.equals(macPc)) {
                throw new IllegalArgumentException(
                        "La caja " + nombreCaja(caja)
                                + " solo puede abrirse en la terminal vinculada"
                                + (caja.getNombre_terminal() != null && !caja.getNombre_terminal().isBlank()
                                ? " (" + caja.getNombre_terminal() + ")"
                                : "")
                                + ".");
            }
        }
    }

    private static String nombreCaja(Caja caja) {
        return caja.getNombre() != null ? caja.getNombre() : ("#" + caja.getId_caja());
    }

    /**
     * Aplica o limpia el vínculo. Cadena vacía en {@code macAddress} desvincula.
     * Si ambos campos vienen {@code null} (omitidos), no se modifica el vínculo.
     * Sincroniza también {@code financiero.terminal.id_caja}.
     */
    private void aplicarVinculoTerminal(Caja entidad, String macAddress, String nombreTerminal, Long excludeId) {
        boolean macPresenteEnInput = macAddress != null;
        boolean nombrePresenteEnInput = nombreTerminal != null;

        if (!macPresenteEnInput && !nombrePresenteEnInput) {
            return;
        }

        if (macPresenteEnInput) {
            String mac = MacAddressUtil.normalize(macAddress);
            if (mac == null) {
                entidad.setMac_address(null);
                if (!nombrePresenteEnInput || nombreTerminal.isBlank()) {
                    entidad.setNombre_terminal(null);
                }
            } else if (repository.existsByMacAddressExcluding(mac, excludeId)) {
                throw new IllegalArgumentException(
                        "Ya existe otra caja vinculada a la terminal con MAC " + mac);
            } else {
                entidad.setMac_address(mac);
            }
        }

        if (nombrePresenteEnInput) {
            String nombre = nombreTerminal.trim();
            entidad.setNombre_terminal(nombre.isEmpty() ? null : nombre);
        }
    }

    private void sincronizarTerminalConCaja(Caja caja, String macAnterior) {
        String macNueva = MacAddressUtil.safeNormalize(caja.getMac_address());
        Long idCaja = caja.getId_caja();

        if (macAnterior != null && (macNueva == null || !macAnterior.equals(macNueva))) {
            terminalRepository.findByMacAddress(macAnterior).ifPresent(t -> {
                if (t.getCaja() != null && (idCaja == null || t.getCaja().getId_caja().equals(idCaja))) {
                    t.setCaja(null);
                    terminalRepository.save(t);
                }
            });
        }

        if (macNueva == null || idCaja == null) {
            return;
        }

        terminalRepository.findByCajaId(idCaja).ifPresent(otra -> {
            if (!macNueva.equals(otra.getMac_address())) {
                otra.setCaja(null);
                terminalRepository.save(otra);
            }
        });

        Terminal terminal = terminalRepository.findByMacAddress(macNueva).orElseGet(() ->
                Terminal.builder()
                        .mac_address(macNueva)
                        .hostname(caja.getNombre_terminal())
                        .nombre(caja.getNombre_terminal())
                        .fecha_registro(LocalDateTime.now())
                        .ultima_conexion(LocalDateTime.now())
                        .activa(true)
                        .build());
        terminal.setCaja(caja);
        if (caja.getNombre_terminal() != null && !caja.getNombre_terminal().isBlank()) {
            terminal.setNombre(caja.getNombre_terminal());
        }
        terminalRepository.save(terminal);
    }

    private String mensajeMacDuplicada(DataIntegrityViolationException ex) {
        String detalle = String.valueOf(ex.getMostSpecificCause().getMessage());
        if (detalle.contains("uq_caja_mac_address")) {
            return "Ya existe otra caja vinculada a esa dirección MAC";
        }
        return "No se pudo guardar la caja por un conflicto de datos";
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
