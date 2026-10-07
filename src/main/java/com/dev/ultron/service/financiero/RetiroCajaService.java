package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.CatalogoDenominaciones;
import com.dev.ultron.domain.financiero.MovimientoCaja;
import com.dev.ultron.domain.financiero.RetiroCaja;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.financiero.input.RetiroCajaInput;
import com.dev.ultron.dto.financiero.mapper.RetiroCajaMapper;
import com.dev.ultron.dto.financiero.output.RetiroCajaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.MovimientoCajaRepository;
import com.dev.ultron.repository.financiero.RetiroCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.personas.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RetiroCajaService {

    private static final int MAX_OBSERVACION = 500;
    private static final int MAX_CONCEPTO = 255;
    private static final Map<String, String> SIMBOLOS = Map.of("PYG", "Gs.", "BRL", "R$", "USD", "US$");

    private final RetiroCajaRepository repository;
    private final RetiroCajaMapper mapper;
    private final SesionCajaRepository sesionCajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final CajaRepository cajaRepository;
    private final ArqueoCajaService arqueoCajaService;

    /** No se puede retirar más efectivo del que debería haber en la caja en esa moneda. */
    @Transactional
    public RetiroCajaOutput registrar(RetiroCajaInput input) {
        if (input == null || input.getIdSesionCaja() == null) {
            throw new IllegalArgumentException("Debe indicar la sesión de caja");
        }
        SesionCaja sesion = sesionCajaRepository.bloquearPorId(input.getIdSesionCaja())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Sesión de caja no encontrada con id: " + input.getIdSesionCaja()));
        if (!"ABIERTA".equalsIgnoreCase(sesion.getEstado())) {
            throw new IllegalArgumentException("Solo se puede retirar dinero con la caja abierta");
        }

        String moneda = CatalogoDenominaciones.codigo(input.getMoneda());
        if (!ArqueoCajaService.MONEDAS.contains(moneda)) {
            throw new IllegalArgumentException("Moneda no válida para el retiro: " + input.getMoneda());
        }
        BigDecimal monto = validarMonto(input.getMonto(), moneda);
        String observacion = validarObservacion(input.getObservacion());

        if (input.getIdUsuarioResponsable() == null) {
            throw new IllegalArgumentException("Seleccioná el usuario responsable del retiro");
        }
        Usuario responsable = usuarioRepository.findById(input.getIdUsuarioResponsable())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + input.getIdUsuarioResponsable()));
        if (Boolean.FALSE.equals(responsable.getActivo())) {
            throw new IllegalArgumentException("El usuario responsable está inactivo");
        }

        BigDecimal disponible = arqueoCajaService.esperado(sesion, moneda);
        if (monto.compareTo(disponible) > 0) {
            throw new IllegalArgumentException("No hay suficiente efectivo en la caja. Disponible: "
                    + SIMBOLOS.get(moneda) + " " + disponible.max(BigDecimal.ZERO)
                    .setScale(CatalogoDenominaciones.escala(moneda), RoundingMode.HALF_UP).toPlainString());
        }

        RetiroCaja retiro = repository.save(RetiroCaja.builder()
                .sesionCaja(sesion)
                .moneda(moneda)
                .monto(monto)
                .observacion(observacion)
                .responsable(responsable)
                .registradoPor(usuarioLogueado())
                .fecha(LocalDateTime.now())
                .build());

        registrarMovimiento(sesion, retiro);
        return mapper.toOutput(retiro);
    }

    @Transactional(readOnly = true)
    public List<RetiroCajaOutput> listarPorSesion(Long idSesionCaja) {
        if (idSesionCaja == null) {
            throw new IllegalArgumentException("Debe indicar la sesión de caja");
        }
        return repository.listarPorSesion(idSesionCaja).stream().map(mapper::toOutput).toList();
    }

    private void registrarMovimiento(SesionCaja sesion, RetiroCaja retiro) {
        Caja caja = sesion.getCaja();
        String concepto = "Retiro de caja: " + retiro.getObservacion();
        movimientoCajaRepository.save(MovimientoCaja.builder()
                .caja(caja)
                .tipo("RETIRO")
                .monto(retiro.getMonto())
                .moneda(retiro.getMoneda())
                .concepto(concepto.length() > MAX_CONCEPTO ? concepto.substring(0, MAX_CONCEPTO) : concepto)
                .fecha(retiro.getFecha())
                .persona(retiro.getResponsable().getFuncionario() != null
                        ? retiro.getResponsable().getFuncionario().getPersona()
                        : null)
                .maletin(sesion.getMaletin())
                .sesionCaja(sesion)
                .referencia("RET-" + retiro.getId_retiro_caja())
                .build());

        if ("PYG".equals(retiro.getMoneda()) && caja != null) {
            BigDecimal saldo = caja.getSaldo_actual() != null ? caja.getSaldo_actual() : BigDecimal.ZERO;
            caja.setSaldo_actual(saldo.subtract(retiro.getMonto()));
            cajaRepository.save(caja);
        }
    }

    private static BigDecimal validarMonto(BigDecimal monto, String moneda) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto del retiro debe ser mayor a cero");
        }
        int escala = CatalogoDenominaciones.escala(moneda);
        if (monto.stripTrailingZeros().scale() > escala) {
            throw new IllegalArgumentException(escala == 0
                    ? "El monto en guaraníes no lleva decimales"
                    : "El monto admite hasta 2 decimales");
        }
        return monto.setScale(escala, RoundingMode.UNNECESSARY);
    }

    private static String validarObservacion(String observacion) {
        String texto = observacion != null ? observacion.trim() : "";
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Escribí el motivo del retiro en la observación");
        }
        if (texto.length() > MAX_OBSERVACION) {
            throw new IllegalArgumentException("La observación admite hasta " + MAX_OBSERVACION + " caracteres");
        }
        return texto;
    }

    private Usuario usuarioLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            return null;
        }
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}
