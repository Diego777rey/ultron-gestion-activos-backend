package com.dev.ultron.controller.personas;

import com.dev.ultron.dto.personas.output.PersonaOutput;
import com.dev.ultron.dto.personas.mapper.PersonaMapper;
import com.dev.ultron.service.personas.PersonaService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PersonaGraphQLController {

    private final PersonaService personaService;

    public PersonaGraphQLController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @QueryMapping
    public PersonaOutput buscarPersonaPorDocumento(@Argument String documento) {
        return personaService.buscarPorDocumento(documento)
                .map(PersonaMapper::toOutput)
                .orElse(null);
    }
}
