package com.dev.ultron.controller.personas;

import com.dev.ultron.dto.personas.output.ContribuyenteRucOutput;
import com.dev.ultron.service.personas.ConsultaRucService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ConsultaRucGraphQLController {

    private final ConsultaRucService consultaRucService;

    @QueryMapping
    public ContribuyenteRucOutput consultarRuc(@Argument String ruc) {
        return consultaRucService.consultar(ruc);
    }
}
