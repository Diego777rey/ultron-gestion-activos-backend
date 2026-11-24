package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.Terminal;
import com.dev.ultron.dto.financiero.input.RegistrarTerminalInput;
import com.dev.ultron.dto.financiero.output.TerminalOutput;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.TerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TerminalServiceTest {

    private final TerminalRepository terminalRepository = mock(TerminalRepository.class);
    private final CajaRepository cajaRepository = mock(CajaRepository.class);
    private final TerminalService service = new TerminalService(terminalRepository, cajaRepository);

    @BeforeEach
    void setUp() {
        when(terminalRepository.save(any(Terminal.class))).thenAnswer(inv -> {
            Terminal t = inv.getArgument(0);
            if (t.getId_terminal() == null) {
                t.setId_terminal(10L);
            }
            return t;
        });
        when(cajaRepository.save(any(Caja.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void registrarCreaTerminalNueva() {
        when(terminalRepository.findByMacAddress("AA:BB:CC:DD:EE:FF")).thenReturn(Optional.empty());

        TerminalOutput out = service.registrar(RegistrarTerminalInput.builder()
                .macAddress("aa-bb-cc-dd-ee-ff")
                .hostname("PC-CAJA-1")
                .plataforma("win32")
                .build());

        assertEquals("AA:BB:CC:DD:EE:FF", out.getMacAddress());
        assertEquals("PC-CAJA-1", out.getHostname());
        assertEquals("win32", out.getPlataforma());
        assertNotNull(out.getUltimaConexion());
    }

    @Test
    void asignarGuardaMacEnLaCaja() {
        Terminal terminal = Terminal.builder()
                .id_terminal(10L)
                .mac_address("AA:BB:CC:DD:EE:FF")
                .hostname("PC-CAJA-1")
                .nombre("Mostrador")
                .activa(true)
                .build();
        Caja caja = Caja.builder().id_caja(5L).nombre("CAJA 1").activa(true).build();
        when(terminalRepository.findById(10L)).thenReturn(Optional.of(terminal));
        when(cajaRepository.findById(5L)).thenReturn(Optional.of(caja));
        when(terminalRepository.findByCajaId(5L)).thenReturn(Optional.empty());

        TerminalOutput out = service.asignarCaja(10L, 5L);

        assertEquals(5L, out.getIdCaja());
        assertEquals("CAJA 1", out.getCajaNombre());
        assertEquals("AA:BB:CC:DD:EE:FF", caja.getMac_address());
        assertEquals("Mostrador", caja.getNombre_terminal());
    }

    @Test
    void desasignarLimpiaMacDeLaCaja() {
        Caja caja = Caja.builder()
                .id_caja(5L)
                .nombre("CAJA 1")
                .mac_address("AA:BB:CC:DD:EE:FF")
                .nombre_terminal("Mostrador")
                .activa(true)
                .build();
        Terminal terminal = Terminal.builder()
                .id_terminal(10L)
                .mac_address("AA:BB:CC:DD:EE:FF")
                .activa(true)
                .caja(caja)
                .build();
        when(terminalRepository.findById(10L)).thenReturn(Optional.of(terminal));

        TerminalOutput out = service.desasignarCaja(10L);

        assertNull(out.getIdCaja());
        assertNull(caja.getMac_address());
        assertNull(caja.getNombre_terminal());
    }

    @Test
    void registrarSinMacFalla() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(RegistrarTerminalInput.builder().macAddress("").build()));
    }
}
