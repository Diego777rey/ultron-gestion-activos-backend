package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.input.RegistrarTerminalInput;
import com.dev.ultron.dto.financiero.input.TerminalInput;
import com.dev.ultron.dto.financiero.output.TerminalOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.financiero.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TerminalGraphQLController {

    private final TerminalService service;

    @MutationMapping
    public TerminalOutput registrarTerminal(@Argument RegistrarTerminalInput input) {
        return service.registrar(input);
    }

    @QueryMapping
    public PageResponse<TerminalOutput> listarTerminalesPaginado(
            @Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public TerminalOutput buscarTerminalPorId(@Argument Long id) {
        return service.findById(id);
    }

    @QueryMapping
    public TerminalOutput buscarTerminalPorMac(@Argument String macAddress) {
        return service.findByMac(macAddress);
    }

    @MutationMapping
    public TerminalOutput actualizarTerminal(@Argument Long id, @Argument TerminalInput input) {
        return service.actualizar(id, input);
    }

    @MutationMapping
    public TerminalOutput asignarCajaATerminal(@Argument Long idTerminal, @Argument Long idCaja) {
        return service.asignarCaja(idTerminal, idCaja);
    }

    @MutationMapping
    public TerminalOutput desasignarCajaDeTerminal(@Argument Long idTerminal) {
        return service.desasignarCaja(idTerminal);
    }
}
