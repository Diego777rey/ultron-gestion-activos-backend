package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.ConteoDenominacion;
import com.dev.ultron.domain.financiero.Maletin;
import com.dev.ultron.domain.financiero.MovimientoCaja;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.personas.Persona;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
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
    private final MovimientoCajaRepository movimientoCajaRepository;

    public SesionCajaService(
            SesionCajaRepository repository,
            SesionCajaMapper mapper,
            CajaRepository cajaRepository,
            MaletinRepository maletinRepository,
            PersonaRepository personaRepository,
            MovimientoCajaRepository movimientoCajaRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.cajaRepository = cajaRepository;
        this.maletinRepository = maletinRepository;
        this.personaRepository = personaRepository;
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
        if (repository.existsPorCajaYEstado(caja.getId_caja(), "ABIERTA")) {
            throw new IllegalArgumentException("La caja ya tiene una sesión abierta");
        }

        Maletin maletin = maletinRepository.findById(input.getIdMaletin())
                .orElseThrow(() -> new EntityNotFoundException("Maletín no encontrado con id: " + input.getIdMaletin()));
        if (Boolean.FALSE.equals(maletin.getActivo())) {
            throw new IllegalArgumentException("El maletín seleccionado no está activo");
        }
        if (Boolean.TRUE.equals(maletin.getAbierto())) {
            throw new IllegalArgumentException("El maletín ya está abierto en otra sesión");
        }
        if (repository.existsPorMaletinYEstado(maletin.getId_maletin(), "ABIERTA")) {
            throw new IllegalArgumentException("El maletín ya está asociado a una sesión abierta");
        }
        if (caja.getSector() != null && maletin.getSector() != null
                && !caja.getSector().getId_sector().equals(maletin.getSector().getId_sector())) {
            throw new IllegalArgumentException("El maletín debe pertenecer al mismo sector que la caja");
        }

        Persona persona = null;
        if (input.getIdPersona() != null) {
            persona = personaRepository.findById(input.getIdPersona())
                    .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + input.getIdPersona()));
        }

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
        sesion = guardar(sesion);

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
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<SesionCajaOutput> findAllPaginated(int page, int size, String filter) {
        return new PageResponse<>(repository.buscar(filter, PageRequest.of(page, size)).map(mapper::toOutput));
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
