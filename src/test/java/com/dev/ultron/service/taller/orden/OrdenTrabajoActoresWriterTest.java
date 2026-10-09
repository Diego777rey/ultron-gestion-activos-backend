package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.patrimonio.Equipo;
import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.dto.taller.input.OrdenTrabajoInput;
import com.dev.ultron.repository.personas.FuncionarioRepository;
import com.dev.ultron.repository.personas.UsuarioRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import com.dev.ultron.service.patrimonio.EquipoService;
import com.dev.ultron.service.patrimonio.VehiculoService;
import com.dev.ultron.service.personas.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrdenTrabajoActoresWriterTest {

    private final ClienteService clientes = mock(ClienteService.class);
    private final EquipoService equipos = mock(EquipoService.class);
    private final OrdenTrabajoActoresWriter writer = new OrdenTrabajoActoresWriter(
            clientes,
            mock(VehiculoService.class),
            equipos,
            mock(FuncionarioRepository.class),
            mock(SectorRepository.class),
            mock(UsuarioRepository.class),
            mock(OrdenTrabajoCajaResolver.class));

    private final Cliente cliente = Cliente.builder().id_cliente(1L).build();
    private final Vehiculo vitz = Vehiculo.builder().id_bien(7L).cliente(cliente).build();
    private final Equipo ecu = equipo(10L, "ECU", vitz);
    private final Equipo tablero = equipo(11L, "TABLERO", vitz);

    @BeforeEach
    void setUp() {
        when(clientes.buscarPorIdOrThrow(1L)).thenReturn(cliente);
        when(equipos.buscarPorIdOrThrow(10L)).thenReturn(ecu);
        when(equipos.buscarPorIdOrThrow(11L)).thenReturn(tablero);
    }

    @Test
    void recepcionaVariosEquiposSinRepetirYTomaElVehiculoDondeEstanMontados() {
        OrdenTrabajo orden = new OrdenTrabajo();

        writer.aplicar(orden, recepcion("EQUIPO", null, List.of(10L, 11L, 10L)), false);

        assertEquals(List.of(ecu, tablero), orden.getEquipos());
        assertEquals(vitz, orden.getVehiculo());
    }

    @Test
    void siLosEquiposEstanEnVehiculosDistintosNoAsignaVehiculo() {
        Vehiculo otro = Vehiculo.builder().id_bien(8L).cliente(cliente).build();
        tablero.setVehiculo(otro);
        OrdenTrabajo orden = new OrdenTrabajo();

        writer.aplicar(orden, recepcion("EQUIPO", null, List.of(10L, 11L)), false);

        assertEquals(2, orden.getEquipos().size());
        assertNull(orden.getVehiculo());
    }

    @Test
    void aceptaElIdEquipoDeClientesAnteriores() {
        OrdenTrabajo orden = new OrdenTrabajo();

        writer.aplicar(orden, recepcion("EQUIPO", 11L, null), false);

        assertEquals(List.of(tablero), orden.getEquipos());
    }

    @Test
    void laRecepcionDeVehiculoQuitaLosEquipos() {
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.getEquipos().add(ecu);

        writer.aplicar(orden, recepcion("VEHICULO", null, List.of(10L)), false);

        assertTrue(orden.getEquipos().isEmpty());
    }

    @Test
    void rechazaUnEquipoDeOtroCliente() {
        Cliente otroCliente = Cliente.builder().id_cliente(2L).build();
        when(equipos.buscarPorIdOrThrow(12L)).thenReturn(equipo(12L, "LLAVE", null, otroCliente));
        OrdenTrabajo orden = new OrdenTrabajo();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> writer.aplicar(orden, recepcion("EQUIPO", null, List.of(10L, 12L)), false));

        assertEquals("El equipo LLAVE no pertenece al cliente seleccionado", error.getMessage());
    }

    private Equipo equipo(Long id, String tipo, Vehiculo vehiculo) {
        return equipo(id, tipo, vehiculo, cliente);
    }

    private static Equipo equipo(Long id, String tipo, Vehiculo vehiculo, Cliente cliente) {
        return Equipo.builder().id_equipo(id).tipoEquipo(tipo).vehiculo(vehiculo).cliente(cliente).build();
    }

    private static OrdenTrabajoInput recepcion(String tipo, Long idEquipo, List<Long> idsEquipos) {
        return new OrdenTrabajoInput(null, null, 1L, tipo, null, idEquipo, idsEquipos,
                null, null, null, null, null, null, null, null);
    }
}
