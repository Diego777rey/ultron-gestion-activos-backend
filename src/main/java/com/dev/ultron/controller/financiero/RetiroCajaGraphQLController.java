package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.input.RetiroCajaInput;
import com.dev.ultron.dto.financiero.output.RetiroCajaOutput;
import com.dev.ultron.service.financiero.RetiroCajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RetiroCajaGraphQLController {

    private final RetiroCajaService service;

    @QueryMapping
    public List<RetiroCajaOutput> listarRetirosPorSesion(@Argument Long idSesionCaja) {
        return service.listarPorSesion(idSesionCaja);
    }

    @MutationMapping
    public RetiroCajaOutput registrarRetiroCaja(@Argument RetiroCajaInput input) {
        return service.registrar(input);
    }
}
