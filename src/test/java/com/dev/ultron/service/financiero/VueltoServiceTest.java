package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Cotizacion;
import com.dev.ultron.dto.financiero.output.DenominacionVueltoOutput;
import com.dev.ultron.dto.financiero.output.VueltoOutput;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VueltoServiceTest {

    private final CotizacionService cotizaciones = mock(CotizacionService.class);
    private final VueltoService service = new VueltoService(cotizaciones);

    // ---------- Todo en guaraníes ----------

    @Test
    void desglosaElVueltoDeMayorAMenorConLaMenorCantidadDePiezas() {
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), new BigDecimal("1000000"));

        assertTrue(vuelto.getSuficiente());
        assertEquals("PYG", vuelto.getMonedaRecibida());
        assertEquals("PYG", vuelto.getMonedaVuelto());
        assertEquals(new BigDecimal("434000"), vuelto.getVuelto());
        assertEquals(new BigDecimal("434000"), vuelto.getVueltoPyg());
        assertEquals(0, vuelto.getFaltante().signum());
        assertEquals(0, vuelto.getResiduo().signum());
        assertTrue(vuelto.getDesgloseDisponible());
        assertDesglose(vuelto.getDesglose(),
                pieza("100000", 4),
                pieza("20000", 1),
                pieza("10000", 1),
                pieza("2000", 2));
    }

    @Test
    void usaMonedasParaElVueltoChico() {
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("8350"), new BigDecimal("10000"));

        assertEquals(new BigDecimal("1650"), vuelto.getVuelto());
        assertDesglose(vuelto.getDesglose(),
                pieza("1000", 1),
                pieza("500", 1),
                pieza("100", 1),
                pieza("50", 1));
        assertEquals("MONEDA", vuelto.getDesglose().get(0).getTipo());
    }

    @Test
    void informaElResiduoQueNoSePuedeEntregarConDenominaciones() {
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("9975"), new BigDecimal("10000"));

        assertEquals(new BigDecimal("25"), vuelto.getVuelto());
        assertTrue(vuelto.getDesglose().isEmpty());
        assertEquals(new BigDecimal("25"), vuelto.getResiduo());
    }

    @Test
    void pagoExactoNoTieneVuelto() {
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("50000"), new BigDecimal("50000"));

        assertTrue(vuelto.getSuficiente());
        assertEquals(0, vuelto.getVuelto().signum());
        assertTrue(vuelto.getDesglose().isEmpty());
    }

    @Test
    void montoInsuficienteInformaCuantoFalta() {
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), new BigDecimal("500000"));

        assertFalse(vuelto.getSuficiente());
        assertEquals(0, vuelto.getVuelto().signum());
        assertEquals(new BigDecimal("66000"), vuelto.getFaltante());
        assertTrue(vuelto.getDesglose().isEmpty());
    }

    @Test
    void sinMontoRecibidoSoloDevuelveSugerencias() {
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), null);

        assertFalse(vuelto.getSuficiente());
        assertNull(vuelto.getMontoRecibido());
        assertEquals(new BigDecimal("566000"), vuelto.getFaltante());
        assertEquals(
                List.of(
                        new BigDecimal("566000"),
                        new BigDecimal("570000"),
                        new BigDecimal("580000"),
                        new BigDecimal("600000"),
                        new BigDecimal("1000000")),
                vuelto.getMontosSugeridos());
    }

    @Test
    void lasSugerenciasNoRepitenRedondeosNiElExacto() {
        List<BigDecimal> sugeridos = service.sugerirMontos(new BigDecimal("100000"));

        assertEquals(
                List.of(new BigDecimal("100000"), new BigDecimal("500000"), new BigDecimal("1000000")),
                sugeridos);
    }

    // ---------- El cliente paga en otra moneda ----------

    @Test
    void pagoEnDolaresConVueltoEnGuaranies() {
        when(cotizaciones.buscarActiva("USD")).thenReturn(cotizacion("USD", "7300"));

        // Total Gs. 566.000 = US$ 77,53. El cliente paga US$ 100 = Gs. 730.000.
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), new BigDecimal("100"), "USD", "PYG");

        assertEquals("USD", vuelto.getMonedaRecibida());
        assertEquals(new BigDecimal("7300"), vuelto.getCotizacionRecibida());
        assertEquals(new BigDecimal("77.53"), vuelto.getTotalMonedaRecibida());
        assertEquals(new BigDecimal("100.00"), vuelto.getMontoRecibido());
        assertEquals(new BigDecimal("730000"), vuelto.getMontoRecibidoPyg());
        assertTrue(vuelto.getSuficiente());
        assertEquals("PYG", vuelto.getMonedaVuelto());
        assertNull(vuelto.getCotizacionVuelto());
        assertEquals(new BigDecimal("164000"), vuelto.getVuelto());
        assertEquals(new BigDecimal("164000"), vuelto.getVueltoPyg());
        assertDesglose(vuelto.getDesglose(),
                pieza("100000", 1),
                pieza("50000", 1),
                pieza("10000", 1),
                pieza("2000", 2));
    }

    @Test
    void sugerenciasEnMonedaExtranjeraRedondeanALaSiguienteUnidad() {
        when(cotizaciones.buscarActiva("USD")).thenReturn(cotizacion("USD", "7300"));

        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), null, "USD", null);

        assertEquals(
                List.of(
                        new BigDecimal("77.53"),
                        new BigDecimal("78.00"),
                        new BigDecimal("80.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("200.00")),
                vuelto.getMontosSugeridos());
        assertEquals(new BigDecimal("77.53"), vuelto.getFaltante());
    }

    @Test
    void faltanteEnMonedaExtranjeraCuandoNoAlcanza() {
        when(cotizaciones.buscarActiva("BRL")).thenReturn(cotizacion("BRL", "1400"));

        // Total Gs. 566.000 = R$ 404,29. El cliente paga R$ 400 = Gs. 560.000.
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), new BigDecimal("400"), "BRL", "PYG");

        assertFalse(vuelto.getSuficiente());
        assertEquals(0, vuelto.getVuelto().signum());
        assertEquals(new BigDecimal("4.29"), vuelto.getFaltante());
        assertEquals(new BigDecimal("560000"), vuelto.getMontoRecibidoPyg());
    }

    // ---------- El cajero devuelve en otra moneda ----------

    @Test
    void pagoEnDolaresConVueltoEnDolaresDesglosaBilletesYCentavos() {
        when(cotizaciones.buscarActiva("USD")).thenReturn(cotizacion("USD", "7300"));

        // Vuelto Gs. 164.000 = US$ 22,47
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), new BigDecimal("100"), "USD", "USD");

        assertEquals("USD", vuelto.getMonedaVuelto());
        assertEquals(new BigDecimal("7300"), vuelto.getCotizacionVuelto());
        assertEquals(new BigDecimal("164000"), vuelto.getVueltoPyg());
        assertEquals(new BigDecimal("22.47"), vuelto.getVuelto());
        assertTrue(vuelto.getDesgloseDisponible());
        assertEquals(new BigDecimal("0.00"), vuelto.getResiduo());
        assertDesglose(vuelto.getDesglose(),
                pieza("20.00", 1),
                pieza("2.00", 1),
                pieza("0.25", 1),
                pieza("0.10", 2),
                pieza("0.01", 2));
    }

    @Test
    void pagoEnGuaraniesConVueltoEnReales() {
        when(cotizaciones.buscarActiva("BRL")).thenReturn(cotizacion("BRL", "1400"));

        // Vuelto Gs. 434.000 = R$ 310,00
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), new BigDecimal("1000000"), "PYG", "BRL");

        assertEquals("PYG", vuelto.getMonedaRecibida());
        assertEquals("BRL", vuelto.getMonedaVuelto());
        assertEquals(new BigDecimal("434000"), vuelto.getVueltoPyg());
        assertEquals(new BigDecimal("310.00"), vuelto.getVuelto());
        assertDesglose(vuelto.getDesglose(),
                pieza("200.00", 1),
                pieza("100.00", 1),
                pieza("10.00", 1));
    }

    @Test
    void vueltoEnRealesInformaResiduoPorDebajoDeCincoCentavos() {
        when(cotizaciones.buscarActiva("BRL")).thenReturn(cotizacion("BRL", "1000"));

        // Vuelto Gs. 1.030 = R$ 1,03 → R$ 1 + residuo 0,03
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("8970"), new BigDecimal("10000"), "PYG", "BRL");

        assertEquals(new BigDecimal("1.03"), vuelto.getVuelto());
        assertDesglose(vuelto.getDesglose(), pieza("1.00", 1));
        assertEquals(new BigDecimal("0.03"), vuelto.getResiduo());
    }

    @Test
    void monedaDeVueltoSinCatalogoDevuelveElMontoSinDesglose() {
        when(cotizaciones.buscarActiva("EUR")).thenReturn(cotizacion("EUR", "8000"));

        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), new BigDecimal("1000000"), "PYG", "EUR");

        assertEquals("EUR", vuelto.getMonedaVuelto());
        assertEquals(new BigDecimal("54.25"), vuelto.getVuelto());
        assertFalse(vuelto.getDesgloseDisponible());
        assertTrue(vuelto.getDesglose().isEmpty());
        assertEquals(0, vuelto.getResiduo().signum());
    }

    @Test
    void nombresLibresDeCotizacionSeMapeanAlCatalogo() {
        when(cotizaciones.buscarActiva("Dólar")).thenReturn(cotizacion("Dólar", "7300"));

        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("566000"), new BigDecimal("1000000"), "PYG", "Dólar");

        assertEquals("Dólar", vuelto.getMonedaVuelto());
        assertTrue(vuelto.getDesgloseDisponible());
        assertEquals(new BigDecimal("59.45"), vuelto.getVuelto());
    }

    @Test
    void monedaSinCotizacionActivaEsRechazada() {
        when(cotizaciones.buscarActiva(anyString())).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.calcularVuelto(new BigDecimal("1000"), new BigDecimal("1"), "EUR", null));
        assertThrows(IllegalArgumentException.class,
                () -> service.calcularVuelto(new BigDecimal("1000"), new BigDecimal("2000"), null, "EUR"));
    }

    @Test
    void monedaVaciaONulaSeTrataComoGuaranies() {
        VueltoOutput vuelto = service.calcularVuelto(new BigDecimal("1000"), new BigDecimal("2000"), " ", null);

        assertEquals("PYG", vuelto.getMonedaRecibida());
        assertEquals("PYG", vuelto.getMonedaVuelto());
        assertNull(vuelto.getCotizacionRecibida());
        assertEquals(new BigDecimal("1000"), vuelto.getVuelto());
    }

    @Test
    void rechazaImportesNegativos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calcularVuelto(new BigDecimal("-1"), null));
        assertThrows(IllegalArgumentException.class,
                () -> service.calcularVuelto(new BigDecimal("1000"), new BigDecimal("-1")));
    }

    private static void assertDesglose(List<DenominacionVueltoOutput> desglose, Object[]... esperado) {
        assertEquals(esperado.length, desglose.size(), "cantidad de denominaciones: " + desglose);
        for (int i = 0; i < esperado.length; i++) {
            DenominacionVueltoOutput pieza = desglose.get(i);
            BigDecimal valor = new BigDecimal((String) esperado[i][0]);
            int cantidad = (Integer) esperado[i][1];
            assertEquals(valor, pieza.getValor(), "valor en posición " + i);
            assertEquals(cantidad, pieza.getCantidad(), "cantidad de " + valor);
            assertEquals(valor.multiply(BigDecimal.valueOf(cantidad)), pieza.getSubtotal(), "subtotal de " + valor);
        }
    }

    private static Object[] pieza(String valor, int cantidad) {
        return new Object[]{valor, cantidad};
    }

    private static Cotizacion cotizacion(String moneda, String valor) {
        return Cotizacion.builder().moneda(moneda).valor(new BigDecimal(valor)).activa(true).build();
    }
}
