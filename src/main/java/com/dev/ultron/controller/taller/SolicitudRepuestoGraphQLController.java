package com.dev.ultron.controller.taller;

import com.dev.ultron.dto.taller.input.SolicitudRepuestoInput;
import com.dev.ultron.dto.taller.output.SolicitudRepuestoOutput;
import com.dev.ultron.service.taller.SolicitudRepuestoService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class SolicitudRepuestoGraphQLController {

    private final SolicitudRepuestoService service;

    public SolicitudRepuestoGraphQLController(SolicitudRepuestoService service) {
        this.service = service;
    }

    @QueryMapping
    public List<SolicitudRepuestoOutput> listarSolicitudesRepuestoPorOrden(@Argument Long idOrden) {
        return service.listarPorOrden(idOrden);
    }

    @QueryMapping
    public SolicitudRepuestoOutput buscarSolicitudRepuestoPorId(@Argument Long id) {
        return service.buscarOutputPorId(id);
    }

    @MutationMapping
    public SolicitudRepuestoOutput crearSolicitudRepuesto(
            @Argument Long idOrden, @Argument SolicitudRepuestoInput input) {
        return service.crear(idOrden, input);
    }

    @MutationMapping
    public SolicitudRepuestoOutput aprobarSolicitudRepuesto(@Argument Long id) {
        return service.aprobar(id);
    }

    @MutationMapping
    public SolicitudRepuestoOutput rechazarSolicitudRepuesto(
            @Argument Long id, @Argument String motivo) {
        return service.rechazar(id, motivo);
    }
}
