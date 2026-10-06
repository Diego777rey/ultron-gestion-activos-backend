package com.dev.ultron.controller.patrimonio;

import com.dev.ultron.dto.patrimonio.input.EquipoInput;
import com.dev.ultron.dto.patrimonio.output.EquipoOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.patrimonio.EquipoService;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller GraphQL para el módulo de Equipos.
 */
@Controller
public class EquipoGraphQLController {

    private final EquipoService equipoService;

    public EquipoGraphQLController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    // ==================== QUERIES ====================

    @QueryMapping
    public List<EquipoOutput> listarEquipos() {
        return equipoService.listarTodosEquipos();
    }

    @QueryMapping
    public PageResponse<EquipoOutput> listarEquiposPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return equipoService.listarEquiposPaginado(page, size, filter);
    }

    @QueryMapping
    public PageResponse<EquipoOutput> listarEquiposPorClientePaginado(
            @Argument Long idCliente,
            @Argument int page,
            @Argument int size,
            @Argument String filter) {
        return equipoService.listarEquiposPorClientePaginado(idCliente, page, size, filter);
    }

    @QueryMapping
    public EquipoOutput buscarEquipoPorId(@Argument Long id) {
        return equipoService.buscarEquipoPorId(id);
    }

    // ==================== MUTATIONS ====================

    @MutationMapping
    public EquipoOutput registrarEquipo(@Argument EquipoInput input) {
        return equipoService.registrarEquipo(input);
    }

    @MutationMapping
    public EquipoOutput actualizarEquipo(@Argument Long id, @Argument EquipoInput input) {
        return equipoService.actualizarEquipo(id, input);
    }

    @MutationMapping
    public Boolean eliminarEquipo(@Argument Long id) {
        return equipoService.eliminarEquipo(id);
    }
}
