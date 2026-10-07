package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.ConteoDenominacion;
import com.dev.ultron.domain.financiero.Maletin;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.financiero.input.AbrirCajaInput;
import com.dev.ultron.dto.financiero.input.CerrarCajaInput;
import com.dev.ultron.dto.financiero.input.ConteoDenominacionInput;
import com.dev.ultron.dto.financiero.mapper.SesionCajaMapper;
import com.dev.ultron.dto.financiero.output.ArqueoMonedaOutput;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.MaletinRepository;
import com.dev.ultron.repository.financiero.MovimientoCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.personas.PersonaRepository;
import com.dev.ultron.repository.personas.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SesionCajaServiceTest {

    private final SesionCajaRepository repository = mock(SesionCajaRepository.class);
    private final CajaRepository cajaRepository = mock(CajaRepository.class);
    private final MaletinRepository maletinRepository = mock(MaletinRepository.class);
    private final PersonaRepository personaRepository = mock(PersonaRepository.class);
    private final ArqueoCajaService arqueoCajaService = mock(ArqueoCajaService.class);
    private final SesionCajaService service = new SesionCajaService(
            repository,
            mock(SesionCajaMapper.class),
            cajaRepository,
            maletinRepository,
            personaRepository,
            mock(UsuarioRepository.class),
            mock(MovimientoCajaRepository.class),
            arqueoCajaService);

    private final Sector sector = Sector.builder().id_sector(1L).build();
    private final Caja caja = Caja.builder().id_caja(1L).nombre("Caja 1").sector(sector).activa(true).build();
    private final Maletin maletin = Maletin.builder()
            .id_maletin(5L).nombre("M-01").sector(sector).activo(true).abierto(false).build();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        when(cajaRepository.findById(1L)).thenReturn(Optional.of(caja));
        when(maletinRepository.findById(5L)).thenReturn(Optional.of(maletin));
        when(personaRepository.findById(9L)).thenReturn(Optional.of(Persona.builder().id_persona(9L).build()));
        when(repository.findPorCajaYEstado(anyLong(), anyString())).thenReturn(Optional.empty());
        when(repository.save(any(SesionCaja.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void laDiferenciaDeAperturaEsLoContadoMenosElUltimoCierreDelMaletin() {
        SesionCaja cierreDeLaManana = SesionCaja.builder()
                .id_sesion_caja(7L)
                .estado("CERRADA")
                .montoFinalPyg(new BigDecimal("8000"))
                .montoFinalBrl(new BigDecimal("86"))
                .montoFinalUsd(BigDecimal.ZERO)
                .fechaCierre(LocalDateTime.of(2026, 10, 2, 19, 8))
                .build();
        when(repository.findUltimoCierrePorMaletin(any(), any())).thenReturn(List.of(cierreDeLaManana));

        service.abrirCaja(apertura(conteo("PYG", "1000", 3), conteo("PYG", "5000", 4)));

        SesionCaja abierta = sesionGuardada();
        assertSame(cierreDeLaManana, abierta.getSesionAnterior());
        assertEquals(new BigDecimal("23000"), abierta.getMontoInicialPyg());
        assertEquals(0, new BigDecimal("15000").compareTo(abierta.getDiferenciaPyg()));
        assertEquals(0, new BigDecimal("-86").compareTo(abierta.getDiferenciaBrl()));
        assertEquals(0, abierta.getDiferenciaUsd().signum());
    }

    @Test
    void sinCierreAnteriorNoHayDiferencia() {
        when(repository.findUltimoCierrePorMaletin(any(), any())).thenReturn(List.of());

        service.abrirCaja(apertura(conteo("PYG", "10000", 2)));

        SesionCaja abierta = sesionGuardada();
        assertNull(abierta.getSesionAnterior());
        assertEquals(0, abierta.getDiferenciaPyg().signum());
        assertEquals(0, abierta.getDiferenciaBrl().signum());
        assertEquals(0, abierta.getDiferenciaUsd().signum());
    }

    @Test
    void cerrarGuardaElEsperadoYLaDiferenciaDeArqueoSinTocarLaDeApertura() {
        SesionCaja abierta = SesionCaja.builder()
                .id_sesion_caja(8L)
                .caja(caja)
                .maletin(maletin)
                .estado("ABIERTA")
                .montoInicialPyg(new BigDecimal("23000"))
                .totalVentasPyg(new BigDecimal("6256000"))
                .diferenciaPyg(new BigDecimal("15000"))
                .conteos(new ArrayList<ConteoDenominacion>())
                .build();
        when(repository.bloquearPorId(8L)).thenReturn(Optional.of(abierta));
        when(arqueoCajaService.calcular(abierta)).thenReturn(List.of(
                esperado("PYG", "320000"),
                esperado("BRL", "50"),
                esperado("USD", "0")));

        CerrarCajaInput input = new CerrarCajaInput();
        input.setIdSesionCaja(8L);
        input.setConteos(List.of(conteo("PYG", "100000", 3), conteo("BRL", "10", 6)));
        service.cerrarCaja(input);

        assertEquals("CERRADA", abierta.getEstado());
        assertEquals(new BigDecimal("300000"), abierta.getMontoFinalPyg());
        assertEquals(new BigDecimal("320000"), abierta.getEsperadoCierrePyg());
        assertEquals(0, new BigDecimal("-20000").compareTo(abierta.getDiferenciaArqueoPyg()));
        assertEquals(0, new BigDecimal("10").compareTo(abierta.getDiferenciaArqueoBrl()));
        assertEquals(0, abierta.getDiferenciaArqueoUsd().signum());
        assertEquals(new BigDecimal("15000"), abierta.getDiferenciaPyg());
    }

    private static ArqueoMonedaOutput esperado(String moneda, String monto) {
        return ArqueoMonedaOutput.builder().moneda(moneda).esperado(new BigDecimal(monto)).build();
    }

    private SesionCaja sesionGuardada() {
        ArgumentCaptor<SesionCaja> captor = ArgumentCaptor.forClass(SesionCaja.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private static AbrirCajaInput apertura(ConteoDenominacionInput... conteos) {
        AbrirCajaInput input = new AbrirCajaInput();
        input.setIdCaja(1L);
        input.setIdMaletin(5L);
        input.setIdPersona(9L);
        input.setConteos(List.of(conteos));
        return input;
    }

    private static ConteoDenominacionInput conteo(String moneda, String valor, int cantidad) {
        ConteoDenominacionInput conteo = new ConteoDenominacionInput();
        conteo.setMoneda(moneda);
        conteo.setValorDenominacion(new BigDecimal(valor));
        conteo.setCantidad(cantidad);
        return conteo;
    }
}
