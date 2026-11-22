package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.Terminal;
import com.dev.ultron.dto.financiero.input.RegistrarTerminalInput;
import com.dev.ultron.dto.financiero.input.TerminalInput;
import com.dev.ultron.dto.financiero.output.TerminalOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.TerminalRepository;
import com.dev.ultron.util.MacAddressUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Service
public class TerminalService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final TerminalRepository repository;
    private final CajaRepository cajaRepository;

    public TerminalService(TerminalRepository repository, CajaRepository cajaRepository) {
        this.repository = repository;
        this.cajaRepository = cajaRepository;
    }

    /**
     * Upsert por MAC: la PC Electron llama esto al abrir la app.
     * Actualiza hostname/plataforma y {@code ultima_conexion}.
     */
    @Transactional
    public TerminalOutput registrar(RegistrarTerminalInput input) {
        if (input == null || input.getMacAddress() == null || input.getMacAddress().isBlank()) {
            throw new IllegalArgumentException("La dirección MAC es obligatoria para registrar la terminal");
        }
        String mac = MacAddressUtil.normalize(input.getMacAddress());
        LocalDateTime ahora = LocalDateTime.now();

        Terminal terminal = repository.findByMacAddress(mac).orElse(null);
        if (terminal == null) {
            terminal = Terminal.builder()
                    .mac_address(mac)
                    .hostname(trimToNull(input.getHostname()))
                    .nombre(trimToNull(input.getHostname()))
                    .plataforma(trimToNull(input.getPlataforma()))
                    .fecha_registro(ahora)
                    .ultima_conexion(ahora)
                    .activa(true)
                    .build();
        } else {
            terminal.setHostname(trimToNull(input.getHostname()));
            terminal.setPlataforma(trimToNull(input.getPlataforma()));
            terminal.setUltima_conexion(ahora);
            if (terminal.getActiva() == null) {
                terminal.setActiva(true);
            }
            if (terminal.getNombre() == null || terminal.getNombre().isBlank()) {
                terminal.setNombre(trimToNull(input.getHostname()));
            }
        }
        return toOutput(repository.save(terminal));
    }

    @Transactional(readOnly = true)
    public PageResponse<TerminalOutput> findAllPaginated(int page, int size, String filter) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ultima_conexion"));
        return new PageResponse<>(repository.buscar(filter, pageable).map(this::toOutput));
    }

    @Transactional(readOnly = true)
    public TerminalOutput findById(Long id) {
        return toOutput(buscarOrThrow(id));
    }

    @Transactional(readOnly = true)
    public TerminalOutput findByMac(String macAddress) {
        String mac = MacAddressUtil.normalize(macAddress);
        return repository.findByMacAddress(mac).map(this::toOutput).orElse(null);
    }

    @Transactional
    public TerminalOutput actualizar(Long id, TerminalInput input) {
        Terminal terminal = buscarOrThrow(id);
        if (input.getNombre() != null) {
            terminal.setNombre(trimToNull(input.getNombre()));
        }
        if (input.getActiva() != null) {
            terminal.setActiva(input.getActiva());
        }
        return toOutput(repository.save(terminal));
    }

    /**
     * Asigna una caja a esta terminal de forma remota.
     * Persiste la MAC en {@code caja.mac_address} para auditoría y validación al abrir.
     */
    @Transactional
    public TerminalOutput asignarCaja(Long idTerminal, Long idCaja) {
        if (idCaja == null) {
            throw new IllegalArgumentException("Debe indicar la caja a habilitar en la terminal");
        }
        Terminal terminal = buscarOrThrow(idTerminal);
        if (Boolean.FALSE.equals(terminal.getActiva())) {
            throw new IllegalArgumentException("La terminal está desactivada");
        }

        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new EntityNotFoundException("Caja no encontrada con id: " + idCaja));
        if (Boolean.FALSE.equals(caja.getActiva())) {
            throw new IllegalArgumentException("La caja seleccionada no está activa");
        }

        // Si otra terminal ya tenía esta caja, liberarla.
        repository.findByCajaId(idCaja).ifPresent(otra -> {
            if (!otra.getId_terminal().equals(idTerminal)) {
                otra.setCaja(null);
                repository.save(otra);
            }
        });

        // Si esta terminal tenía otra caja, limpiar la MAC de esa caja.
        liberarMacDeCaja(terminal.getCaja());

        // Si la caja tenía otra MAC / otra terminal, limpiar vínculo previo.
        if (caja.getMac_address() != null && !caja.getMac_address().isBlank()) {
            String macPrevia = MacAddressUtil.safeNormalize(caja.getMac_address());
            if (macPrevia != null && !macPrevia.equals(terminal.getMac_address())) {
                repository.findByMacAddress(macPrevia).ifPresent(t -> {
                    if (t.getCaja() != null && t.getCaja().getId_caja().equals(caja.getId_caja())) {
                        t.setCaja(null);
                        repository.save(t);
                    }
                });
            }
        }

        terminal.setCaja(caja);
        caja.setMac_address(terminal.getMac_address());
        caja.setNombre_terminal(nombreVisible(terminal));
        cajaRepository.save(caja);
        return toOutput(repository.save(terminal));
    }

    @Transactional
    public TerminalOutput desasignarCaja(Long idTerminal) {
        Terminal terminal = buscarOrThrow(idTerminal);
        liberarMacDeCaja(terminal.getCaja());
        terminal.setCaja(null);
        return toOutput(repository.save(terminal));
    }

    private void liberarMacDeCaja(Caja caja) {
        if (caja == null) {
            return;
        }
        caja.setMac_address(null);
        caja.setNombre_terminal(null);
        cajaRepository.save(caja);
    }

    private Terminal buscarOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terminal no encontrada con id: " + id));
    }

    private TerminalOutput toOutput(Terminal t) {
        Caja caja = t.getCaja();
        return TerminalOutput.builder()
                .id_terminal(t.getId_terminal())
                .macAddress(t.getMac_address())
                .hostname(t.getHostname())
                .nombre(t.getNombre())
                .plataforma(t.getPlataforma())
                .ultimaConexion(format(t.getUltima_conexion()))
                .fechaRegistro(format(t.getFecha_registro()))
                .activa(t.getActiva())
                .idCaja(caja != null ? caja.getId_caja() : null)
                .cajaNombre(caja != null ? caja.getNombre() : null)
                .build();
    }

    private static String nombreVisible(Terminal terminal) {
        if (terminal.getNombre() != null && !terminal.getNombre().isBlank()) {
            return terminal.getNombre().trim();
        }
        return terminal.getHostname();
    }

    private static String format(LocalDateTime value) {
        return value != null ? value.format(ISO) : null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
