package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.dto.financiero.output.ArqueoMonedaOutput;
import com.dev.ultron.repository.financiero.RetiroCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.financiero.VentaRepository;
import com.dev.ultron.repository.financiero.VentaRepository.CobroEfectivo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArqueoCajaServiceTest {

    private final VentaRepository ventas = mock(VentaRepository.class);
    private final RetiroCajaRepository retiros = mock(RetiroCajaRepository.class);
    private final ArqueoCajaService service =
            new ArqueoCajaService(mock(SesionCajaRepository.class), ventas, retiros);

    @Test
    void esperadoEsAperturaMasCobrosEnEfectivoMenosVueltosYRetirosPorMoneda() {
        SesionCaja sesion = SesionCaja.builder()
                .id_sesion_caja(8L)
                .estado("CERRADA")
                .montoInicialPyg(new BigDecimal("100000"))
                .montoInicialBrl(new BigDecimal("20"))
                .montoInicialUsd(BigDecimal.ZERO)
                .montoFinalPyg(new BigDecimal("190000"))
                .montoFinalBrl(new BigDecimal("70"))
                .montoFinalUsd(new BigDecimal("10"))
                .build();
        when(ventas.cobrosEnEfectivo(8L)).thenReturn(List.of(
                // Pagó 200.000 por 150.000 y recibió 50.000 de vuelto.
                cobro("PYG", "150000", null, "200000", "PYG", "50000"),
                // Sin monto recibido: se asume el exacto.
                cobro("PYG", "40000", null, null, null, null),
                // Pagó R$ 100 y el vuelto se dio en guaraníes.
                cobro("REAL", "90000", "75", "100", "PYG", "20000"),
                cobro("DOLAR", "70000", "10", null, null, null)));
        when(retiros.totalesPorMoneda(8L)).thenReturn(List.of(retirado("PYG", "100000"), retirado("BRL", "50")));

        List<ArqueoMonedaOutput> arqueo = service.calcular(sesion);

        ArqueoMonedaOutput pyg = arqueo.get(0);
        assertEquals("PYG", pyg.getMoneda());
        assertEquals(0, new BigDecimal("240000").compareTo(pyg.getCobrosEfectivo()));
        assertEquals(0, new BigDecimal("70000").compareTo(pyg.getVueltos()));
        assertEquals(0, new BigDecimal("100000").compareTo(pyg.getRetiros()));
        assertEquals(0, new BigDecimal("170000").compareTo(pyg.getEsperado()));
        assertEquals(0, new BigDecimal("20000").compareTo(pyg.getDiferencia()));

        ArqueoMonedaOutput brl = arqueo.get(1);
        assertEquals("BRL", brl.getMoneda());
        assertEquals(0, new BigDecimal("70").compareTo(brl.getEsperado()));
        assertEquals(0, brl.getDiferencia().signum());

        ArqueoMonedaOutput usd = arqueo.get(2);
        assertEquals(0, new BigDecimal("10").compareTo(usd.getEsperado()));
        assertEquals(0, usd.getDiferencia().signum());
    }

    @Test
    void conLaSesionAbiertaNoHayContadoNiDiferencia() {
        SesionCaja sesion = SesionCaja.builder()
                .id_sesion_caja(9L)
                .estado("ABIERTA")
                .montoInicialPyg(new BigDecimal("50000"))
                .build();

        ArqueoMonedaOutput pyg = service.calcular(sesion).get(0);

        assertEquals(0, new BigDecimal("50000").compareTo(pyg.getEsperado()));
        assertNull(pyg.getContado());
        assertNull(pyg.getDiferencia());
    }

    static CobroEfectivo cobro(String moneda, String total, String original, String recibido,
                               String monedaVuelto, String vuelto) {
        return new CobroEfectivo() {
            @Override
            public String getMoneda() {
                return moneda;
            }

            @Override
            public BigDecimal getTotal() {
                return total != null ? new BigDecimal(total) : null;
            }

            @Override
            public BigDecimal getMontoMonedaOriginal() {
                return original != null ? new BigDecimal(original) : null;
            }

            @Override
            public BigDecimal getMontoRecibido() {
                return recibido != null ? new BigDecimal(recibido) : null;
            }

            @Override
            public String getMonedaVuelto() {
                return monedaVuelto;
            }

            @Override
            public BigDecimal getVuelto() {
                return vuelto != null ? new BigDecimal(vuelto) : null;
            }
        };
    }

    static RetiroCajaRepository.TotalPorMoneda retirado(String moneda, String total) {
        return new RetiroCajaRepository.TotalPorMoneda() {
            @Override
            public String getMoneda() {
                return moneda;
            }

            @Override
            public BigDecimal getTotal() {
                return new BigDecimal(total);
            }
        };
    }
}
