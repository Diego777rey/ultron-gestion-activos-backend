package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.ConteoDenominacion;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput.ConteoLinea;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput.ConteoMoneda;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput.DiferenciaMoneda;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput.Retiro;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput.VentasFormaPago;
import com.dev.ultron.dto.financiero.mapper.RetiroCajaMapper;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.financiero.RetiroCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.financiero.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/** Arma el ticket de cierre de caja: conteos, ventas, retiros, arqueo y diferencia con el cierre anterior. */
@Service
@RequiredArgsConstructor
public class TicketCierreCajaService {

    private static final List<String> ORDEN_FORMAS_PAGO = List.of("EFECTIVO", "TARJETA", "TRANSFERENCIA");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final SesionCajaRepository sesionCajaRepository;
    private final VentaRepository ventaRepository;
    private final RetiroCajaRepository retiroCajaRepository;
    private final RetiroCajaMapper retiroCajaMapper;
    private final ArqueoCajaService arqueoCajaService;

    @Transactional(readOnly = true)
    public TicketCierreCajaOutput generar(Long idSesionCaja) {
        if (idSesionCaja == null) {
            throw new IllegalArgumentException("Debe indicar la sesión de caja");
        }
        SesionCaja sesion = sesionCajaRepository.findById(idSesionCaja)
                .orElseThrow(() -> new EntityNotFoundException("Sesión de caja no encontrada con id: " + idSesionCaja));
        if (!"CERRADA".equalsIgnoreCase(sesion.getEstado())) {
            throw new IllegalArgumentException("La caja todavía no se cerró");
        }

        List<VentasFormaPago> ventas = ventasPorFormaPago(idSesionCaja);
        SesionCaja anterior = sesion.getSesionAnterior();

        return TicketCierreCajaOutput.builder()
                .idSesionCaja(sesion.getId_sesion_caja())
                .caja(sesion.getCaja() != null ? sesion.getCaja().getNombre() : null)
                .maletin(sesion.getMaletin() != null ? sesion.getMaletin().getNombre() : null)
                .cajero(nombrePersona(sesion.getPersona()))
                .fechaApertura(formatear(sesion.getFechaApertura()))
                .fechaCierre(formatear(sesion.getFechaCierre()))
                .conteoApertura(conteos(sesion, "APERTURA"))
                .conteoCierre(conteos(sesion, "CIERRE"))
                .cantidadVentas(ventas.stream().mapToInt(VentasFormaPago::getCantidad).sum())
                .totalVentasPyg(ventas.stream().map(VentasFormaPago::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add))
                .ventasPorFormaPago(ventas)
                .retiros(retiros(idSesionCaja))
                .arqueo(arqueoCajaService.calcular(sesion))
                .idSesionAnterior(anterior != null ? anterior.getId_sesion_caja() : null)
                .fechaCierreAnterior(anterior != null ? formatear(anterior.getFechaCierre()) : null)
                .diferencias(List.of(
                        diferencia("PYG", sesion, anterior, SesionCaja::getMontoInicialPyg,
                                SesionCaja::getMontoFinalPyg, SesionCaja::getDiferenciaPyg),
                        diferencia("BRL", sesion, anterior, SesionCaja::getMontoInicialBrl,
                                SesionCaja::getMontoFinalBrl, SesionCaja::getDiferenciaBrl),
                        diferencia("USD", sesion, anterior, SesionCaja::getMontoInicialUsd,
                                SesionCaja::getMontoFinalUsd, SesionCaja::getDiferenciaUsd)))
                .build();
    }

    private List<ConteoMoneda> conteos(SesionCaja sesion, String tipo) {
        List<ConteoDenominacion> delTipo = sesion.getConteos().stream()
                .filter(c -> tipo.equalsIgnoreCase(c.getTipo()))
                .filter(c -> c.getCantidad() != null && c.getCantidad() > 0 && c.getValorDenominacion() != null)
                .toList();
        return ArqueoCajaService.MONEDAS.stream().map(moneda -> {
            List<ConteoLinea> lineas = delTipo.stream()
                    .filter(c -> moneda.equalsIgnoreCase(c.getMoneda()))
                    .sorted(Comparator.comparing(ConteoDenominacion::getValorDenominacion))
                    .map(c -> ConteoLinea.builder()
                            .valor(c.getValorDenominacion())
                            .cantidad(c.getCantidad())
                            .subtotal(c.getValorDenominacion().multiply(BigDecimal.valueOf(c.getCantidad())))
                            .build())
                    .toList();
            BigDecimal total = lineas.stream().map(ConteoLinea::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            return ConteoMoneda.builder().moneda(moneda).lineas(lineas).total(total).build();
        }).toList();
    }

    private List<Retiro> retiros(Long idSesionCaja) {
        return retiroCajaRepository.listarPorSesion(idSesionCaja).stream()
                .map(r -> Retiro.builder()
                        .fecha(r.getFecha() != null ? r.getFecha().format(FECHA_CORTA) : null)
                        .moneda(r.getMoneda())
                        .monto(r.getMonto())
                        .responsable(retiroCajaMapper.nombreUsuario(r.getResponsable()))
                        .observacion(r.getObservacion())
                        .build())
                .toList();
    }

    private List<VentasFormaPago> ventasPorFormaPago(Long idSesionCaja) {
        return ventaRepository.totalesPorFormaPago(idSesionCaja).stream()
                .map(t -> VentasFormaPago.builder()
                        .formaPago(t.getFormaPago().toUpperCase())
                        .cantidad(t.getCantidad() != null ? t.getCantidad().intValue() : 0)
                        .total(nvl(t.getTotal()))
                        .build())
                .sorted(Comparator.comparingInt(v -> ordenFormaPago(v.getFormaPago())))
                .toList();
    }

    private static int ordenFormaPago(String formaPago) {
        int indice = ORDEN_FORMAS_PAGO.indexOf(formaPago);
        return indice >= 0 ? indice : ORDEN_FORMAS_PAGO.size();
    }

    private static DiferenciaMoneda diferencia(
            String moneda,
            SesionCaja sesion,
            SesionCaja anterior,
            Function<SesionCaja, BigDecimal> apertura,
            Function<SesionCaja, BigDecimal> cierre,
            Function<SesionCaja, BigDecimal> diferencia) {
        return DiferenciaMoneda.builder()
                .moneda(moneda)
                .cierreAnterior(anterior != null ? nvl(cierre.apply(anterior)) : null)
                .apertura(nvl(apertura.apply(sesion)))
                .diferencia(nvl(diferencia.apply(sesion)))
                .build();
    }

    private static String nombrePersona(Persona persona) {
        if (persona == null) {
            return null;
        }
        String nombre = ((persona.getNombre() != null ? persona.getNombre() : "")
                + " "
                + (persona.getApellido() != null ? persona.getApellido() : "")).trim();
        return nombre.isEmpty() ? null : nombre;
    }

    private static String formatear(LocalDateTime fecha) {
        return fecha != null ? fecha.format(FECHA) : null;
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
