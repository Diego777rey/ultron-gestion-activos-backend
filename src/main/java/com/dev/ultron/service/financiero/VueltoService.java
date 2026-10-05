package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.CatalogoDenominaciones;
import com.dev.ultron.domain.financiero.Cotizacion;
import com.dev.ultron.domain.financiero.Denominacion;
import com.dev.ultron.dto.financiero.output.DenominacionVueltoOutput;
import com.dev.ultron.dto.financiero.output.VueltoOutput;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Calcula el vuelto de un cobro en efectivo. El cliente puede pagar en guaraníes
 * o en cualquier moneda con cotización activa, y el cajero puede devolver en la
 * moneda que elija. Todo se concilia en guaraníes y el vuelto se desglosa con los
 * billetes y monedas de la divisa elegida. Toda la lógica vive acá; el frontend solo presenta.
 */
@Service
public class VueltoService {

    public static final String PYG = CatalogoDenominaciones.PYG;

    /** Redondeos "cómodos" con los que la gente suele pagar en guaraníes, de menor a mayor. */
    private static final long[] REDONDEOS_PYG = {
            1_000, 5_000, 10_000, 20_000, 50_000, 100_000, 500_000, 1_000_000
    };
    /** Redondeos para monedas extranjeras (unidades enteras de la moneda). */
    private static final long[] REDONDEOS_EXTRANJERA = {1, 5, 10, 20, 50, 100, 200, 500, 1_000};
    private static final int MAX_SUGERENCIAS = 5;

    private final CotizacionService cotizacionService;

    public VueltoService(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    /** Guaraníes por unidad de una moneda y su nombre canónico, resueltos desde las cotizaciones activas. */
    public record Moneda(String nombre, BigDecimal cotizacion) {
        public static final Moneda GUARANI = new Moneda(PYG, null);

        public boolean esGuarani() {
            return cotizacion == null;
        }
    }

    /**
     * @param totalPyg       total a cobrar en guaraníes.
     * @param montoRecibido  lo que entregó el cliente, en {@code monedaRecibida}; {@code null} si aún no se informó.
     * @param monedaRecibida moneda en la que paga el cliente; vacío equivale a PYG.
     * @param monedaVuelto   moneda en la que se entrega el vuelto; vacío equivale a PYG.
     */
    public VueltoOutput calcularVuelto(BigDecimal totalPyg, BigDecimal montoRecibido,
                                       String monedaRecibida, String monedaVuelto) {
        return calcular(totalPyg, montoRecibido, resolver(monedaRecibida), resolver(monedaVuelto));
    }

    /** Variante todo en guaraníes. */
    public VueltoOutput calcularVuelto(BigDecimal totalPyg, BigDecimal montoRecibido) {
        return calcular(totalPyg, montoRecibido, Moneda.GUARANI, Moneda.GUARANI);
    }

    /** Busca la cotización activa de la moneda. PYG (o vacío) no necesita cotización. */
    public Moneda resolver(String moneda) {
        if (CatalogoDenominaciones.esGuarani(moneda)) {
            return Moneda.GUARANI;
        }
        Cotizacion cotizacion = cotizacionService.buscarActiva(moneda);
        if (cotizacion == null || cotizacion.getValor() == null || cotizacion.getValor().signum() <= 0) {
            throw new IllegalArgumentException(
                    "No se encontró una cotización activa para la moneda " + moneda.trim());
        }
        return new Moneda(cotizacion.getMoneda(), cotizacion.getValor());
    }

    /** Cálculo con las cotizaciones ya resueltas. */
    public VueltoOutput calcular(BigDecimal totalPyg, BigDecimal montoRecibido, Moneda recibida, Moneda vuelto) {
        if (totalPyg == null || totalPyg.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total en guaraníes debe ser mayor o igual a cero");
        }
        if (montoRecibido != null && montoRecibido.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto recibido no puede ser negativo");
        }
        if (recibida == null) {
            recibida = Moneda.GUARANI;
        }
        if (vuelto == null) {
            vuelto = Moneda.GUARANI;
        }

        BigDecimal total = totalPyg.setScale(0, RoundingMode.HALF_UP);
        int escalaRecibida = CatalogoDenominaciones.escala(recibida.nombre());
        int escalaVuelto = CatalogoDenominaciones.escala(vuelto.nombre());
        BigDecimal totalEnRecibida = aMoneda(total, recibida, escalaRecibida);
        Optional<List<Denominacion>> catalogoVuelto = CatalogoDenominaciones.de(vuelto.nombre());

        VueltoOutput.VueltoOutputBuilder salida = VueltoOutput.builder()
                .totalPyg(total)
                .monedaRecibida(recibida.nombre())
                .cotizacionRecibida(recibida.cotizacion())
                .totalMonedaRecibida(totalEnRecibida)
                .montosSugeridos(recibida.esGuarani() ? sugerirMontos(total) : sugerirMontosExtranjera(totalEnRecibida))
                .monedaVuelto(vuelto.nombre())
                .cotizacionVuelto(vuelto.cotizacion())
                .desgloseDisponible(catalogoVuelto.isPresent())
                .vueltoPyg(BigDecimal.ZERO)
                .vuelto(cero(escalaVuelto))
                .desglose(List.of())
                .residuo(cero(escalaVuelto));

        if (montoRecibido == null) {
            return salida.faltante(totalEnRecibida).suficiente(false).build();
        }

        BigDecimal recibido = montoRecibido.setScale(escalaRecibida, RoundingMode.HALF_UP);
        BigDecimal recibidoPyg = aGuaranies(recibido, recibida);
        salida.montoRecibido(recibido).montoRecibidoPyg(recibidoPyg);

        BigDecimal diferenciaPyg = recibidoPyg.subtract(total);
        if (diferenciaPyg.signum() < 0) {
            BigDecimal faltante = recibida.esGuarani()
                    ? diferenciaPyg.negate()
                    : totalEnRecibida.subtract(recibido).max(cero(escalaRecibida));
            return salida.faltante(faltante).suficiente(false).build();
        }

        BigDecimal vueltoEnMoneda = aMoneda(diferenciaPyg, vuelto, escalaVuelto);
        List<DenominacionVueltoOutput> desglose = catalogoVuelto
                .map(catalogo -> desglosar(vueltoEnMoneda, catalogo, escalaVuelto))
                .orElse(List.of());
        BigDecimal entregado = desglose.stream()
                .map(DenominacionVueltoOutput::getSubtotal)
                .reduce(cero(escalaVuelto), BigDecimal::add);

        return salida
                .faltante(cero(escalaRecibida))
                .suficiente(true)
                .vueltoPyg(diferenciaPyg)
                .vuelto(vueltoEnMoneda)
                .desglose(desglose)
                .residuo(catalogoVuelto.isPresent() ? vueltoEnMoneda.subtract(entregado) : cero(escalaVuelto))
                .build();
    }

    /**
     * Desglose voraz de mayor a menor, operando en la unidad mínima de la divisa
     * (guaraníes o centavos). Los catálogos son canónicos, así que el voraz entrega
     * siempre la menor cantidad de piezas.
     */
    public List<DenominacionVueltoOutput> desglosar(BigDecimal monto, List<Denominacion> catalogo, int escala) {
        List<DenominacionVueltoOutput> desglose = new ArrayList<>();
        long restante = Math.max(monto.setScale(escala, RoundingMode.DOWN).movePointRight(escala).longValueExact(), 0);
        for (Denominacion denominacion : catalogo) {
            long valor = denominacion.enMinimos(escala);
            if (valor <= 0 || restante < valor) {
                continue;
            }
            long cantidad = restante / valor;
            restante -= cantidad * valor;
            desglose.add(DenominacionVueltoOutput.builder()
                    .valor(denominacion.valor().setScale(escala, RoundingMode.UNNECESSARY))
                    .tipo(denominacion.tipo().name())
                    .cantidad((int) cantidad)
                    .subtotal(BigDecimal.valueOf(cantidad * valor, escala))
                    .build());
        }
        return desglose;
    }

    /**
     * Importes en guaraníes con los que el cliente suele pagar: el exacto y los
     * redondeos hacia arriba más cercanos (siguiente 1.000, 5.000, 10.000, ...).
     */
    public List<BigDecimal> sugerirMontos(BigDecimal totalPyg) {
        if (totalPyg == null || totalPyg.signum() <= 0) {
            return List.of();
        }
        long total = totalPyg.setScale(0, RoundingMode.HALF_UP).longValueExact();
        return sugerir(BigDecimal.valueOf(total), total, 1, REDONDEOS_PYG, 0);
    }

    /**
     * Importes en moneda extranjera: el exacto (con centavos) y los redondeos
     * hacia arriba a la siguiente unidad, 5, 10, 20, 50, 100...
     */
    public List<BigDecimal> sugerirMontosExtranjera(BigDecimal totalEnMoneda) {
        if (totalEnMoneda == null || totalEnMoneda.signum() <= 0) {
            return List.of();
        }
        BigDecimal exacto = totalEnMoneda.setScale(2, RoundingMode.HALF_UP);
        long centavos = exacto.movePointRight(2).longValueExact();
        return sugerir(exacto, centavos, 100, REDONDEOS_EXTRANJERA, 2);
    }

    private static List<BigDecimal> sugerir(BigDecimal exacto, long totalEnMinimos, long factorPaso,
                                            long[] pasos, int escalaSalida) {
        TreeSet<Long> redondeos = new TreeSet<>();
        for (long paso : pasos) {
            long pasoMinimos = paso * factorPaso;
            long redondeado = ((totalEnMinimos + pasoMinimos - 1) / pasoMinimos) * pasoMinimos;
            if (redondeado > totalEnMinimos) {
                redondeos.add(redondeado);
            }
        }
        List<BigDecimal> sugeridos = new ArrayList<>();
        sugeridos.add(exacto);
        for (Long redondeado : redondeos) {
            if (sugeridos.size() >= MAX_SUGERENCIAS) {
                break;
            }
            sugeridos.add(BigDecimal.valueOf(redondeado, escalaSalida));
        }
        return sugeridos;
    }

    private static BigDecimal aGuaranies(BigDecimal monto, Moneda moneda) {
        if (moneda.esGuarani()) {
            return monto.setScale(0, RoundingMode.HALF_UP);
        }
        return monto.multiply(moneda.cotizacion()).setScale(0, RoundingMode.HALF_UP);
    }

    private static BigDecimal aMoneda(BigDecimal montoPyg, Moneda moneda, int escala) {
        if (moneda.esGuarani()) {
            return montoPyg.setScale(0, RoundingMode.HALF_UP);
        }
        return montoPyg.divide(moneda.cotizacion(), escala, RoundingMode.HALF_UP);
    }

    private static BigDecimal cero(int escala) {
        return BigDecimal.ZERO.setScale(escala);
    }
}
