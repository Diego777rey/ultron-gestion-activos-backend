package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.ConteoDenominacion;
import com.dev.ultron.domain.financiero.Maletin;
import com.dev.ultron.domain.financiero.RetiroCaja;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.financiero.output.ArqueoMonedaOutput;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput.ConteoMoneda;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput.DiferenciaMoneda;
import com.dev.ultron.dto.financiero.mapper.RetiroCajaMapperImpl;
import com.dev.ultron.repository.financiero.RetiroCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.financiero.VentaRepository;
import com.dev.ultron.repository.financiero.VentaRepository.TotalVentasPorFormaPago;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketCierreCajaServiceTest {

    private final SesionCajaRepository sesiones = mock(SesionCajaRepository.class);
    private final VentaRepository ventas = mock(VentaRepository.class);
    private final RetiroCajaRepository retiros = mock(RetiroCajaRepository.class);
    private final ArqueoCajaService arqueo = new ArqueoCajaService(sesiones, ventas, retiros);
    private final TicketCierreCajaService service =
            new TicketCierreCajaService(sesiones, ventas, retiros, new RetiroCajaMapperImpl(), arqueo);

    @Test
    void armaElTicketConConteosVentasYDiferenciaConElCierreAnterior() {
        SesionCaja anterior = SesionCaja.builder()
                .id_sesion_caja(7L)
                .estado("CERRADA")
                .montoFinalPyg(new BigDecimal("8000"))
                .montoFinalBrl(new BigDecimal("86"))
                .montoFinalUsd(BigDecimal.ZERO)
                .fechaCierre(LocalDateTime.of(2026, 10, 2, 19, 8))
                .build();
        SesionCaja sesion = SesionCaja.builder()
                .id_sesion_caja(8L)
                .caja(Caja.builder().nombre("Caja 1").build())
                .maletin(Maletin.builder().nombre("M-01").build())
                .persona(Persona.builder().nombre("Diego").apellido("Maidana").build())
                .sesionAnterior(anterior)
                .estado("CERRADA")
                .montoInicialPyg(new BigDecimal("23000"))
                .montoInicialBrl(BigDecimal.ZERO)
                .montoInicialUsd(BigDecimal.ZERO)
                .diferenciaPyg(new BigDecimal("15000"))
                .diferenciaBrl(new BigDecimal("-86"))
                .diferenciaUsd(BigDecimal.ZERO)
                .fechaApertura(LocalDateTime.of(2026, 10, 2, 19, 8))
                .fechaCierre(LocalDateTime.of(2026, 10, 7, 19, 30))
                .conteos(new ArrayList<>(List.of(
                        conteo("APERTURA", "PYG", "5000", 2),
                        conteo("APERTURA", "PYG", "1000", 3),
                        conteo("APERTURA", "PYG", "2000", 5),
                        conteo("CIERRE", "PYG", "100000", 1),
                        conteo("CIERRE", "BRL", "10", 1),
                        conteo("CIERRE", "BRL", "1", 0))))
                .build();
        sesion.setMontoFinalPyg(new BigDecimal("175000"));
        when(sesiones.findById(8L)).thenReturn(Optional.of(sesion));
        when(ventas.totalesPorFormaPago(8L)).thenReturn(List.of(
                total("TARJETA", 1, "56000"),
                total("EFECTIVO", 3, "200000")));
        when(ventas.cobrosEnEfectivo(8L)).thenReturn(List.of(
                ArqueoCajaServiceTest.cobro("PYG", "200000", null, null, null, null)));
        when(retiros.totalesPorMoneda(8L)).thenReturn(List.of(ArqueoCajaServiceTest.retirado("PYG", "50000")));
        when(retiros.listarPorSesion(8L)).thenReturn(List.of(RetiroCaja.builder()
                .moneda("PYG")
                .monto(new BigDecimal("50000"))
                .observacion("Deposito banco")
                .responsable(Usuario.builder().username("jefe").build())
                .fecha(LocalDateTime.of(2026, 10, 7, 14, 32))
                .build()));

        TicketCierreCajaOutput ticket = service.generar(8L);

        assertEquals(1, ticket.getRetiros().size());
        assertEquals("07/10 14:32", ticket.getRetiros().get(0).getFecha());
        assertEquals("jefe", ticket.getRetiros().get(0).getResponsable());
        ArqueoMonedaOutput arqueoPyg = ticket.getArqueo().get(0);
        assertEquals(0, new BigDecimal("173000").compareTo(arqueoPyg.getEsperado()));
        assertEquals(0, new BigDecimal("175000").compareTo(arqueoPyg.getContado()));
        assertEquals(0, new BigDecimal("2000").compareTo(arqueoPyg.getDiferencia()));

        assertEquals("Caja 1", ticket.getCaja());
        assertEquals("M-01", ticket.getMaletin());
        assertEquals("Diego Maidana", ticket.getCajero());
        assertEquals("02/10/2026 19:08", ticket.getFechaApertura());
        assertEquals("07/10/2026 19:30", ticket.getFechaCierre());

        ConteoMoneda aperturaPyg = ticket.getConteoApertura().get(0);
        assertEquals("PYG", aperturaPyg.getMoneda());
        assertEquals(List.of(new BigDecimal("1000"), new BigDecimal("2000"), new BigDecimal("5000")),
                aperturaPyg.getLineas().stream().map(l -> l.getValor()).toList());
        assertEquals(0, new BigDecimal("23000").compareTo(aperturaPyg.getTotal()));
        assertEquals(List.of("PYG", "BRL", "USD"),
                ticket.getConteoCierre().stream().map(ConteoMoneda::getMoneda).toList());
        ConteoMoneda cierreBrl = ticket.getConteoCierre().get(1);
        assertEquals(1, cierreBrl.getLineas().size());
        assertEquals(0, new BigDecimal("10").compareTo(cierreBrl.getTotal()));
        assertTrue(ticket.getConteoCierre().get(2).getLineas().isEmpty());

        assertEquals(4, ticket.getCantidadVentas());
        assertEquals(0, new BigDecimal("256000").compareTo(ticket.getTotalVentasPyg()));
        assertEquals("EFECTIVO", ticket.getVentasPorFormaPago().get(0).getFormaPago());

        assertEquals(7L, ticket.getIdSesionAnterior());
        assertEquals("02/10/2026 19:08", ticket.getFechaCierreAnterior());
        DiferenciaMoneda pyg = ticket.getDiferencias().get(0);
        assertEquals(0, new BigDecimal("8000").compareTo(pyg.getCierreAnterior()));
        assertEquals(0, new BigDecimal("23000").compareTo(pyg.getApertura()));
        assertEquals(0, new BigDecimal("15000").compareTo(pyg.getDiferencia()));
        assertEquals(0, new BigDecimal("-86").compareTo(ticket.getDiferencias().get(1).getDiferencia()));
    }

    @Test
    void sinCierreAnteriorLaDiferenciaNoTieneReferencia() {
        SesionCaja sesion = SesionCaja.builder()
                .id_sesion_caja(1L)
                .estado("CERRADA")
                .montoInicialPyg(new BigDecimal("200000"))
                .diferenciaPyg(BigDecimal.ZERO)
                .build();
        when(sesiones.findById(1L)).thenReturn(Optional.of(sesion));
        when(ventas.totalesPorFormaPago(1L)).thenReturn(List.of());

        TicketCierreCajaOutput ticket = service.generar(1L);

        assertNull(ticket.getIdSesionAnterior());
        assertNull(ticket.getDiferencias().get(0).getCierreAnterior());
        assertEquals(0, ticket.getCantidadVentas());
        assertEquals(0, ticket.getTotalVentasPyg().signum());
    }

    @Test
    void rechazaSesionesAbiertas() {
        when(sesiones.findById(3L)).thenReturn(Optional.of(SesionCaja.builder().estado("ABIERTA").build()));

        assertThrows(IllegalArgumentException.class, () -> service.generar(3L));
    }

    private static ConteoDenominacion conteo(String tipo, String moneda, String valor, int cantidad) {
        return ConteoDenominacion.builder()
                .tipo(tipo)
                .moneda(moneda)
                .valorDenominacion(new BigDecimal(valor))
                .cantidad(cantidad)
                .build();
    }

    private static TotalVentasPorFormaPago total(String formaPago, long cantidad, String total) {
        return new TotalVentasPorFormaPago() {
            @Override
            public String getFormaPago() {
                return formaPago;
            }

            @Override
            public Long getCantidad() {
                return cantidad;
            }

            @Override
            public BigDecimal getTotal() {
                return new BigDecimal(total);
            }
        };
    }
}
