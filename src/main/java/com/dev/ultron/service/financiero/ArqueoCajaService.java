package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.CatalogoDenominaciones;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.dto.financiero.output.ArqueoMonedaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.financiero.RetiroCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.financiero.VentaRepository;
import com.dev.ultron.repository.financiero.VentaRepository.CobroEfectivo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Efectivo que debería haber en la caja, por moneda:
 * apertura + cobros en efectivo - vueltos entregados - retiros.
 * Tarjeta y transferencia no entran al cajón, así que no cuentan.
 */
@Service
@RequiredArgsConstructor
public class ArqueoCajaService {

    /** Monedas que se cuentan en apertura y cierre, en el orden en que se muestran. */
    public static final List<String> MONEDAS = List.of("PYG", "BRL", "USD");

    private final SesionCajaRepository sesionCajaRepository;
    private final VentaRepository ventaRepository;
    private final RetiroCajaRepository retiroCajaRepository;

    @Transactional(readOnly = true)
    public List<ArqueoMonedaOutput> calcular(Long idSesionCaja) {
        if (idSesionCaja == null) {
            throw new IllegalArgumentException("Debe indicar la sesión de caja");
        }
        SesionCaja sesion = sesionCajaRepository.findById(idSesionCaja)
                .orElseThrow(() -> new EntityNotFoundException("Sesión de caja no encontrada con id: " + idSesionCaja));
        return calcular(sesion);
    }

    /** Con la sesión cerrada también informa lo contado y la diferencia. */
    @Transactional(readOnly = true)
    public List<ArqueoMonedaOutput> calcular(SesionCaja sesion) {
        Map<String, BigDecimal> cobros = new HashMap<>();
        Map<String, BigDecimal> vueltos = new HashMap<>();
        Map<String, BigDecimal> retiros = new HashMap<>();
        Long idSesion = sesion.getId_sesion_caja();

        for (CobroEfectivo cobro : ventaRepository.cobrosEnEfectivo(idSesion)) {
            String moneda = CatalogoDenominaciones.codigo(cobro.getMoneda());
            cobros.merge(moneda, entrada(cobro, moneda), BigDecimal::add);
            if (cobro.getVuelto() != null && cobro.getVuelto().signum() > 0) {
                vueltos.merge(CatalogoDenominaciones.codigo(cobro.getMonedaVuelto()), cobro.getVuelto(), BigDecimal::add);
            }
        }
        for (RetiroCajaRepository.TotalPorMoneda retiro : retiroCajaRepository.totalesPorMoneda(idSesion)) {
            retiros.merge(CatalogoDenominaciones.codigo(retiro.getMoneda()), nvl(retiro.getTotal()), BigDecimal::add);
        }

        boolean cerrada = "CERRADA".equalsIgnoreCase(sesion.getEstado());
        return MONEDAS.stream().map(moneda -> {
            BigDecimal apertura = nvl(apertura(sesion, moneda));
            BigDecimal cobrado = cobros.getOrDefault(moneda, BigDecimal.ZERO);
            BigDecimal vuelto = vueltos.getOrDefault(moneda, BigDecimal.ZERO);
            BigDecimal retirado = retiros.getOrDefault(moneda, BigDecimal.ZERO);
            BigDecimal esperado = apertura.add(cobrado).subtract(vuelto).subtract(retirado);
            BigDecimal contado = cerrada ? nvl(cierre(sesion, moneda)) : null;
            return ArqueoMonedaOutput.builder()
                    .moneda(moneda)
                    .apertura(apertura)
                    .cobrosEfectivo(cobrado)
                    .vueltos(vuelto)
                    .retiros(retirado)
                    .esperado(esperado)
                    .contado(contado)
                    .diferencia(contado != null ? contado.subtract(esperado) : null)
                    .build();
        }).toList();
    }

    /** Efectivo que debería haber ahora en la moneda indicada (código ISO). */
    @Transactional(readOnly = true)
    public BigDecimal esperado(SesionCaja sesion, String moneda) {
        return calcular(sesion).stream()
                .filter(a -> a.getMoneda().equals(moneda))
                .map(ArqueoMonedaOutput::getEsperado)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    /** Si no se informó lo recibido, se asume que el cliente pagó el monto exacto. */
    private static BigDecimal entrada(CobroEfectivo cobro, String moneda) {
        if (cobro.getMontoRecibido() != null) {
            return cobro.getMontoRecibido();
        }
        return "PYG".equals(moneda) ? nvl(cobro.getTotal()) : nvl(cobro.getMontoMonedaOriginal());
    }

    private static BigDecimal apertura(SesionCaja sesion, String moneda) {
        return switch (moneda) {
            case "USD" -> sesion.getMontoInicialUsd();
            case "BRL" -> sesion.getMontoInicialBrl();
            default -> sesion.getMontoInicialPyg();
        };
    }

    private static BigDecimal cierre(SesionCaja sesion, String moneda) {
        return switch (moneda) {
            case "USD" -> sesion.getMontoFinalUsd();
            case "BRL" -> sesion.getMontoFinalBrl();
            default -> sesion.getMontoFinalPyg();
        };
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
