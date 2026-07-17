package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.input.VentaInput;
import com.dev.ultron.dto.financiero.output.VentaOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.financiero.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class VentaGraphQLController {

    private final VentaService service;

    @QueryMapping
    public List<VentaOutput> listarVentas() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<VentaOutput> listarVentasPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public VentaOutput buscarVentaPorId(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public VentaOutput registrarVenta(@Argument VentaInput input) {
        return service.registrarVenta(input);
    }
}
