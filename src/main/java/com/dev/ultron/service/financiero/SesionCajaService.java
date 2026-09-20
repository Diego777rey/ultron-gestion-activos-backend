package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.ConteoDenominacion;
import com.dev.ultron.domain.financiero.Maletin;
import com.dev.ultron.domain.financiero.MovimientoCaja;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.financiero.input.AbrirCajaInput;
import com.dev.ultron.dto.financiero.input.CerrarCajaInput;
import com.dev.ultron.dto.financiero.input.ConteoDenominacionInput;
import com.dev.ultron.dto.financiero.mapper.SesionCajaMapper;
import com.dev.ultron.dto.financiero.output.SesionCajaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.MaletinRepository;
import com.dev.ultron.repository.financiero.MovimientoCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.personas.PersonaRepository;
import com.dev.ultron.repository.personas.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SesionCajaService extends GenericCrudService<SesionCaja, Long> {

    private final SesionCajaRepository repository;
    private final SesionCajaMapper mapper;
    private final CajaRepository cajaRepository;
    private final MaletinRepository maletinRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;

    public SesionCajaService(
            SesionCajaRepository repository,
            SesionCajaMapper mapper,
            CajaRepository cajaRepository,
            MaletinRepository maletinRepository,
            PersonaRepository personaRepository,
            UsuarioRepository usuarioRepository,
            MovimientoCajaRepository movimientoCajaRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.cajaRepository = cajaRepository;
        this.maletinRepository = maletinRepository;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
    }

    @Override
    protected JpaRepository<SesionCaja, Long> getRepository() {
        return repository;
    }

    @Transactional
    public SesionCajaOutput abrirCaja(AbrirCajaInput input) {
        if (input.getIdCaja() == null || input.getIdMaletin() == null) {
            throw new IllegalArgumentException("Debe indicar caja y maletín para abrir la sesión");
        }

        Caja caja = cajaRepository.findById(input.getIdCaja())
                .orElseThrow(() -> new EntityNotFoundException("Caja no encontrada con id: " + input.getIdCaja()));
        if (Boolean.FALSE.equals(caja.getActiva())) {
            throw new IllegalArgumentException("La caja seleccionada no está activa");
        }
        repository.findPorCajaYEstado(caja.getId_caja(), "ABIERTA").ifPresent(abierta -> {
            throw new IllegalArgumentException(mensajeOcupada("La caja ya está abierta", abierta));
        });

        Maletin maletin = maletinRepository.findById(input.getIdMaletin())
                .orElseThrow(() -> new EntityNotFoundException("Maletín no encontrado con id: " + input.getIdMaletin()));
        if (Boolean.FALSE.equals(maletin.getActivo())) {
            throw new IllegalArgumentException("El maletín seleccionado no está activo");
        }
        if (Boolean.TRUE.equals(maletin.getAbierto()) || repository.existsPorMaletinYEstado(maletin.getId_maletin(), "ABIERTA")) {
            SesionCaja ocupada = repository.findAbiertaPorMaletin(maletin.getId_maletin()).orElse(null);
            throw new IllegalArgumentException(
                    ocupada != null
                            ? mensajeOcupada("El maletín ya está en uso", ocupada)
                            : "El maletín ya está en uso. Queda libre cuando se cierre la caja");
        }
        if (caja.getSector() == null || caja.getSector().getId_sector() == null) {
            throw new IllegalArgumentException("La caja no tiene un sector asignado");
        }
        if (maletin.getSector() == null || maletin.getSector().getId_sector() == null) {
            throw new IllegalArgumentException("El maletín no tiene un sector asignado");
        }
        if (!caja.getSector().getId_sector().equals(maletin.getSector().getId_sector())) {
            throw new IllegalArgumentException("El maletín debe pertenecer al mismo sector que la caja");
        }

        Persona persona = resolverPersonaLogueada(input.getIdPersona());

        Map<String, BigDecimal> totales = sumarConteos(input.getConteos());

        SesionCaja sesion = SesionCaja.builder()
                .caja(caja)
                .maletin(maletin)
                .persona(persona)
                .estado("ABIERTA")
                .montoInicialPyg(totales.getOrDefault("PYG", BigDecimal.ZERO))
                .montoInicialUsd(totales.getOrDefault("USD", BigDecimal.ZERO))
                .montoInicialBrl(totales.getOrDefault("BRL", BigDecimal.ZERO))
                .totalVentasPyg(BigDecimal.ZERO)
                .fechaApertura(LocalDateTime.now())
                .build();

        agregarConteos(sesion, "APERTURA", input.getConteos());
        try {
            sesion = guardar(sesion);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(mensajeIntegridadApertura(ex), ex);
        }

        maletin.setAbierto(true);
        if (persona != null) {
            maletin.setResponsable(persona);
        }
        maletinRepository.save(maletin);

        caja.setSaldo_actual(sesion.getMontoInicialPyg());
        cajaRepository.save(caja);

        MovimientoCaja movimiento = MovimientoCaja.builder()
                .caja(caja)
                .tipo("APERTURA")
                .monto(sesion.getMontoInicialPyg())
                .concepto("Apertura de caja")
                .fecha(LocalDateTime.now())
                .persona(persona)
                .moneda("PYG")
                .maletin(maletin)
                .sesionCaja(sesion)
                .referencia("SES-" + sesion.getId_sesion_caja())
                .build();
        movimientoCajaRepository.save(movimiento);

        return mapper.toOutput(sesion);
    }

    @Transactional
    public SesionCajaOutput cerrarCaja(CerrarCajaInput input) {
        if (input.getIdSesionCaja() == null) {
            throw new IllegalArgumentException("Debe indicar la sesión de caja a cerrar");
        }

        SesionCaja sesion = buscarPorIdOrThrow(input.getIdSesionCaja());
        if (!"ABIERTA".equalsIgnoreCase(sesion.getEstado())) {
            throw new IllegalArgumentException("La sesión de caja no está abierta");
        }

        Map<String, BigDecimal> totales = sumarConteos(input.getConteos());
        BigDecimal finalPyg = totales.getOrDefault("PYG", BigDecimal.ZERO);
        BigDecimal finalUsd = totales.getOrDefault("USD", BigDecimal.ZERO);
        BigDecimal finalBrl = totales.getOrDefault("BRL", BigDecimal.ZERO);

        BigDecimal esperadoPyg = nvl(sesion.getMontoInicialPyg()).add(nvl(sesion.getTotalVentasPyg()));
        BigDecimal esperadoUsd = nvl(sesion.getMontoInicialUsd());
        BigDecimal esperadoBrl = nvl(sesion.getMontoInicialBrl());

        sesion.setMontoFinalPyg(finalPyg);
        sesion.setMontoFinalUsd(finalUsd);
        sesion.setMontoFinalBrl(finalBrl);
        sesion.setDiferenciaPyg(finalPyg.subtract(esperadoPyg));
        sesion.setDiferenciaUsd(finalUsd.subtract(esperadoUsd));
        sesion.setDiferenciaBrl(finalBrl.subtract(esperadoBrl));
        sesion.setFechaCierre(LocalDateTime.now());
        sesion.setEstado("CERRADA");

        agregarConteos(sesion, "CIERRE", input.getConteos());
        sesion = actualizar(sesion);

        Maletin maletin = sesion.getMaletin();
        maletin.setAbierto(false);
        if (sesion.getPersona() != null) {
            maletin.setResponsable(sesion.getPersona());
        }
        maletinRepository.save(maletin);

        Caja caja = sesion.getCaja();
        caja.setSaldo_actual(finalPyg);
        cajaRepository.save(caja);

        MovimientoCaja movimiento = MovimientoCaja.builder()
                .caja(caja)
                .tipo("CIERRE")
                .monto(finalPyg)
                .concepto("Cierre de caja")
                .fecha(LocalDateTime.now())
                .persona(sesion.getPersona())
                .moneda("PYG")
                .maletin(maletin)
                .sesionCaja(sesion)
                .referencia("SES-" + sesion.getId_sesion_caja())
                .build();
        movimientoCajaRepository.save(movimiento);

        return mapper.toOutput(sesion);
    }

    @Transactional(readOnly = true)
    public SesionCajaOutput sesionAbierta(Long idCaja) {
        SesionCaja sesion;
        if (idCaja != null) {
            sesion = repository.findPorCajaYEstado(idCaja, "ABIERTA").orElse(null);
        } else {
            sesion = repository.listarPorEstado("ABIERTA", PageRequest.of(0, 1)).stream().findFirst().orElse(null);
        }
        return sesion != null ? mapper.toOutput(sesion) : null;
    }

    @Transactional(readOnly = true)
    public SesionCajaOutput findById(Long id) {
        SesionCaja sesion = buscarPorIdOrThrow(id);
        if (sesion.getConteos() != null) {
            sesion.getConteos().size();
        }
        return mapper.toOutput(sesion);
    }

    @Transactional(readOnly = true)
    public PageResponse<SesionCajaOutput> findAllPaginated(int page, int size, String filter) {
        return findAllPaginated(page, size, filter, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<SesionCajaOutput> findAllPaginated(int page, int size, String filter, Long idCaja) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaApertura"));
        if (idCaja != null) {
            return new PageResponse<>(repository.buscarPorCaja(idCaja, filter, pageable).map(mapper::toOutput));
        }
        return new PageResponse<>(repository.buscar(filter, pageable).map(mapper::toOutput));
    }

    private Persona resolverPersonaLogueada(Long idPersonaInput) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        if (username != null && !username.isBlank() && !"anonymousUser".equalsIgnoreCase(username)) {
            Usuario usuario = usuarioRepository.findByUsernameWithPersona(username)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario logueado"));
            if (usuario.getFuncionario() == null || usuario.getFuncionario().getPersona() == null) {
                throw new IllegalArgumentException("El usuario logueado no tiene un funcionario asociado");
            }
            return usuario.getFuncionario().getPersona();
        }
        if (idPersonaInput != null) {
            return personaRepository.findById(idPersonaInput)
                    .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + idPersonaInput));
        }
        throw new IllegalArgumentException("Debe iniciar sesión para abrir la caja");
    }

    private String mensajeOcupada(String prefijo, SesionCaja sesion) {
        String quien = nombrePersona(sesion.getPersona());
        if (quien != null) {
            return prefijo + " por " + quien;
        }
        return prefijo + ". Queda libre cuando se cierre la caja";
    }

    private String nombrePersona(Persona persona) {
        if (persona == null) {
            return null;
        }
        String nombre = ((persona.getNombre() != null ? persona.getNombre() : "")
                + " "
                + (persona.getApellido() != null ? persona.getApellido() : "")).trim();
        return nombre.isEmpty() ? null : nombre;
    }

    private String mensajeIntegridadApertura(DataIntegrityViolationException ex) {
        String detalle = String.valueOf(ex.getMostSpecificCause().getMessage());
        if (detalle.contains("uq_sesion_caja_abierta")) {
            return "La caja ya está abierta por otro usuario";
        }
        if (detalle.contains("uq_sesion_maletin_abierta")) {
            return "El maletín ya está en uso. Queda libre cuando se cierre la caja";
        }
        return "No se pudo abrir la caja porque caja o maletín ya están en uso";
    }

    private void agregarConteos(SesionCaja sesion, String tipo, List<ConteoDenominacionInput> conteos) {
        if (conteos == null) {
            return;
        }
        for (ConteoDenominacionInput item : conteos) {
            if (item == null || item.getMoneda() == null || item.getValorDenominacion() == null) {
                continue;
            }
            int cantidad = item.getCantidad() != null ? item.getCantidad() : 0;
            ConteoDenominacion conteo = ConteoDenominacion.builder()
                    .sesionCaja(sesion)
                    .tipo(tipo)
                    .moneda(item.getMoneda().toUpperCase())
                    .valorDenominacion(item.getValorDenominacion())
                    .cantidad(cantidad)
                    .build();
            sesion.getConteos().add(conteo);
        }
    }

    private Map<String, BigDecimal> sumarConteos(List<ConteoDenominacionInput> conteos) {
        Map<String, BigDecimal> totales = new HashMap<>();
        if (conteos == null) {
            return totales;
        }
        for (ConteoDenominacionInput item : conteos) {
            if (item == null || item.getMoneda() == null || item.getValorDenominacion() == null) {
                continue;
            }
            String moneda = item.getMoneda().toUpperCase();
            BigDecimal cantidad = BigDecimal.valueOf(item.getCantidad() != null ? item.getCantidad() : 0);
            BigDecimal subtotal = item.getValorDenominacion().multiply(cantidad);
            totales.merge(moneda, subtotal, BigDecimal::add);
        }
        return totales;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
