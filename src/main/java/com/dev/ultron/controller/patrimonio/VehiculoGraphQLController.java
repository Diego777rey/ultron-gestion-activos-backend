package com.dev.ultron.controller.patrimonio;

import com.dev.ultron.dto.patrimonio.input.VehiculoInput;
import com.dev.ultron.dto.patrimonio.output.VehiculoOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.patrimonio.VehiculoService;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller GraphQL para el módulo de Vehículos.
 * Expone las operaciones CRUD a través de queries y mutations.
 */
@Controller
public class VehiculoGraphQLController {

    private final VehiculoService vehiculoService;

    public VehiculoGraphQLController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    // ==================== QUERIES ====================

    @QueryMapping
    public List<VehiculoOutput> listarVehiculos() {
        return vehiculoService.listarTodosVehiculos();
    }

    @QueryMapping
    public PageResponse<VehiculoOutput> listarVehiculosPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return vehiculoService.listarVehiculosPaginado(page, size, filter);
    }

    @QueryMapping
    public PageResponse<VehiculoOutput> listarVehiculosPorClientePaginado(
            @Argument Long idCliente,
            @Argument int page,
            @Argument int size,
            @Argument String filter) {
        return vehiculoService.listarVehiculosPorClientePaginado(idCliente, page, size, filter);
    }

    @QueryMapping
    public VehiculoOutput buscarVehiculoPorId(@Argument Long id) {
        return vehiculoService.buscarVehiculoPorId(id);
    }

    // ==================== MUTATIONS ====================

    @MutationMapping
    public VehiculoOutput registrarVehiculo(@Argument VehiculoInput input) {
        return vehiculoService.registrarVehiculo(input);
    }

    @MutationMapping
    public VehiculoOutput actualizarVehiculo(@Argument Long id, @Argument VehiculoInput input) {
        return vehiculoService.actualizarVehiculo(id, input);
    }

    @MutationMapping
    public Boolean eliminarVehiculo(@Argument Long id) {
        return vehiculoService.eliminarVehiculo(id);
    }
}
