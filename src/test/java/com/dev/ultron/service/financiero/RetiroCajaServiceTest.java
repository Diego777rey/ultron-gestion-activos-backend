package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.MovimientoCaja;
import com.dev.ultron.domain.financiero.RetiroCaja;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.financiero.input.RetiroCajaInput;
import com.dev.ultron.dto.financiero.mapper.RetiroCajaMapperImpl;
import com.dev.ultron.dto.financiero.output.RetiroCajaOutput;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.MovimientoCajaRepository;
import com.dev.ultron.repository.financiero.RetiroCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.personas.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetiroCajaServiceTest {

    private final RetiroCajaRepository repository = mock(RetiroCajaRepository.class);
    private final SesionCajaRepository sesiones = mock(SesionCajaRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final MovimientoCajaRepository movimientos = mock(MovimientoCajaRepository.class);
    private final CajaRepository cajas = mock(CajaRepository.class);
    private final ArqueoCajaService arqueo = mock(ArqueoCajaService.class);
    private final RetiroCajaService service = new RetiroCajaService(
            repository, new RetiroCajaMapperImpl(), sesiones, usuarios, movimientos, cajas, arqueo);

    private final Caja caja = Caja.builder().id_caja(1L).saldo_actual(new BigDecimal("500000")).build();
    private final SesionCaja sesion = SesionCaja.builder().id_sesion_caja(8L).caja(caja).estado("ABIERTA").build();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        when(sesiones.bloquearPorId(8L)).thenReturn(Optional.of(sesion));
        when(usuarios.findById(3L)).thenReturn(Optional.of(Usuario.builder().id(3L).username("jefe").activo(true).build()));
        when(repository.save(any(RetiroCaja.class))).thenAnswer(inv -> inv.getArgument(0));
        when(arqueo.esperado(sesion, "PYG")).thenReturn(new BigDecimal("300000"));
        when(arqueo.esperado(sesion, "BRL")).thenReturn(new BigDecimal("40"));
    }

    @Test
    void registraElRetiroYDescuentaElSaldoEnGuaranies() {
        RetiroCajaOutput retiro = service.registrar(input("Gs", "200000", "  Deposito en banco  "));

        assertEquals("PYG", retiro.getMoneda());
        assertEquals(new BigDecimal("200000"), retiro.getMonto());
        assertEquals("Deposito en banco", retiro.getObservacion());
        assertEquals("jefe", retiro.getResponsableNombre());
        assertEquals(new BigDecimal("300000"), caja.getSaldo_actual());

        ArgumentCaptor<MovimientoCaja> movimiento = ArgumentCaptor.forClass(MovimientoCaja.class);
        verify(movimientos).save(movimiento.capture());
        assertEquals("RETIRO", movimiento.getValue().getTipo());
        assertEquals("PYG", movimiento.getValue().getMoneda());
    }

    @Test
    void enOtraMonedaNoTocaElSaldoEnGuaranies() {
        RetiroCajaOutput retiro = service.registrar(input("Real", "25.50", "Pago proveedor"));

        assertEquals("BRL", retiro.getMoneda());
        assertEquals(new BigDecimal("500000"), caja.getSaldo_actual());
    }

    @Test
    void noPermiteRetirarMasDeLoQueHayEnLaMoneda() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.registrar(input("BRL", "41", "Retiro")));

        assertTrue(error.getMessage().contains("R$ 40.00"));
        verify(repository, never()).save(any());
    }

    @Test
    void validaLosDatosDelRetiro() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(input("PYG", "0", "x")));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(input("PYG", "1000.5", "x")));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(input("PYG", "1000", "   ")));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(input("EUR", "10", "x")));

        RetiroCajaInput sinResponsable = input("PYG", "1000", "x");
        sinResponsable.setIdUsuarioResponsable(null);
        assertThrows(IllegalArgumentException.class, () -> service.registrar(sinResponsable));
    }

    @Test
    void conLaCajaCerradaNoSePuedeRetirar() {
        sesion.setEstado("CERRADA");

        assertThrows(IllegalArgumentException.class, () -> service.registrar(input("PYG", "1000", "x")));
        verify(arqueo, never()).esperado(any(), anyString());
    }

    private static RetiroCajaInput input(String moneda, String monto, String observacion) {
        return RetiroCajaInput.builder()
                .idSesionCaja(8L)
                .moneda(moneda)
                .monto(new BigDecimal(monto))
                .observacion(observacion)
                .idUsuarioResponsable(3L)
                .build();
    }
}
