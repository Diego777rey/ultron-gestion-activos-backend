package com.dev.ultron.controller.taller;

import com.dev.ultron.dto.financiero.output.CajaOutput;
import com.dev.ultron.dto.taller.input.OrdenTrabajoDetalleInput;
import com.dev.ultron.dto.taller.input.OrdenTrabajoInput;
import com.dev.ultron.dto.taller.output.OrdenTrabajoOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.taller.OrdenTrabajoService;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller GraphQL para el módulo de Orden de Trabajo.
 */
@Controller
public class OrdenTrabajoGraphQLController {

    private final OrdenTrabajoService ordenTrabajoService;

    public OrdenTrabajoGraphQLController(OrdenTrabajoService ordenTrabajoService) {
        this.ordenTrabajoService = ordenTrabajoService;
    }

    // ==================== QUERIES ====================

    @QueryMapping
    public PageResponse<OrdenTrabajoOutput> listarOrdenesTrabajoPaginado(
            @Argument int page, @Argument int size, @Argument String filter) {
        return ordenTrabajoService.listarOrdenesPaginado(page, size, filter);
    }

    @QueryMapping
    public OrdenTrabajoOutput buscarOrdenTrabajoPorId(@Argument Long id) {
        return ordenTrabajoService.buscarOrdenPorId(id);
    }

    @QueryMapping
    public PageResponse<OrdenTrabajoOutput> listarOrdenesPorClientePaginado(
            @Argument Long idCliente, @Argument int page, @Argument int size) {
        return ordenTrabajoService.listarOrdenesPorClientePaginado(idCliente, page, size);
    }

    @QueryMapping
    public PageResponse<OrdenTrabajoOutput> listarOrdenesPorVehiculoPaginado(
            @Argument Long idVehiculo, @Argument int page, @Argument int size) {
        return ordenTrabajoService.listarOrdenesPorVehiculoPaginado(idVehiculo, page, size);
    }

    @QueryMapping
    public List<OrdenTrabajoOutput> listarAgendaMecanico(
            @Argument Long idMecanico, @Argument String fechaDesde, @Argument String fechaHasta) {
        return ordenTrabajoService.listarAgendaMecanico(idMecanico, fechaDesde, fechaHasta);
    }

    @QueryMapping
    public List<CajaOutput> listarCajasConSesionAbierta() {
        return ordenTrabajoService.listarCajasConSesionAbierta();
    }

    // ==================== MUTATIONS ====================

    @MutationMapping
    public OrdenTrabajoOutput crearOrdenTrabajo(@Argument OrdenTrabajoInput input) {
        return ordenTrabajoService.crearOrdenTrabajo(input);
    }

    @MutationMapping
    public OrdenTrabajoOutput actualizarOrdenTrabajo(@Argument Long id, @Argument OrdenTrabajoInput input) {
        return ordenTrabajoService.actualizarOrdenTrabajo(id, input);
    }

    @MutationMapping
    public OrdenTrabajoOutput cambiarEtapaOrdenTrabajo(@Argument Long id, @Argument String etapa) {
        return ordenTrabajoService.cambiarEtapa(id, etapa);
    }

    @MutationMapping
    public OrdenTrabajoOutput agregarDetalleOrdenTrabajo(
            @Argument Long idOrden, @Argument OrdenTrabajoDetalleInput input) {
        return ordenTrabajoService.agregarDetalle(idOrden, input);
    }

    @MutationMapping
    public OrdenTrabajoOutput eliminarDetalleOrdenTrabajo(
            @Argument Long idOrden, @Argument Long idDetalle) {
        return ordenTrabajoService.eliminarDetalle(idOrden, idDetalle);
    }

    @MutationMapping
    public OrdenTrabajoOutput enviarOrdenACaja(@Argument Long idOrden, @Argument Long idCaja) {
        return ordenTrabajoService.enviarOrdenACaja(idOrden, idCaja);
    }

    @MutationMapping
    public OrdenTrabajoOutput marcarOrdenFacturada(@Argument Long idOrden) {
        return ordenTrabajoService.marcarOrdenFacturada(idOrden);
    }

    @MutationMapping
    public Boolean eliminarOrdenTrabajo(@Argument Long id) {
        return ordenTrabajoService.eliminarOrden(id);
    }
}
