package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.dto.financiero.mapper.CajaMapper;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.TerminalRepository;
import com.dev.ultron.repository.personas.PersonaRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CajaServiceTerminalTest {

    private final CajaRepository repository = mock(CajaRepository.class);
    private final CajaService service = new CajaService(
            repository,
            mock(CajaMapper.class),
            mock(PersonaRepository.class),
            mock(SectorRepository.class),
            mock(TerminalRepository.class));

    private final Caja cajaMostrador = Caja.builder()
            .id_caja(1L)
            .nombre("MOSTRADOR")
            .mac_address("AA:BB:CC:DD:EE:FF")
            .nombre_terminal("PC-CAJA-1")
            .activa(true)
            .build();

    private final Caja cajaLibre = Caja.builder()
            .id_caja(2L)
            .nombre("LIBRE")
            .activa(true)
            .build();

    @BeforeEach
    void setUp() {
        when(repository.findByMacAddress("AA:BB:CC:DD:EE:FF")).thenReturn(Optional.of(cajaMostrador));
        when(repository.findByMacAddress("11:22:33:44:55:66")).thenReturn(Optional.empty());
    }

    @Test
    void pcConCajaVinculadaPuedeAbrirSuCaja() {
        assertDoesNotThrow(() -> service.validarAperturaEnTerminal(cajaMostrador, "AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void pcNoPuedeAbrirOtraCajaSiYaTieneUnaAsignada() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validarAperturaEnTerminal(cajaLibre, "AA:BB:CC:DD:EE:FF"));
        assertTrue(ex.getMessage().contains("MOSTRADOR"));
    }

    @Test
    void cajaVinculadaRechazaOtraMac() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validarAperturaEnTerminal(cajaMostrador, "11:22:33:44:55:66"));
        assertTrue(ex.getMessage().contains("solo puede abrirse"));
    }

    @Test
    void cajaVinculadaRechazaSinMac() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validarAperturaEnTerminal(cajaMostrador, null));
        assertTrue(ex.getMessage().contains("aplicación de escritorio"));
    }

    @Test
    void cajaSinVinculoSeAbreDesdePcSinCaja() {
        assertDoesNotThrow(() -> service.validarAperturaEnTerminal(cajaLibre, "11:22:33:44:55:66"));
    }
}
