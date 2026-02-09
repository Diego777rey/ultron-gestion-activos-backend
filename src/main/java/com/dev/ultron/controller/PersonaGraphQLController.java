package com.dev.ultron.controller;

import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.RegistroPersonaConBienesInput;
import com.dev.ultron.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PersonaGraphQLController {

    private final PersonaService personaService;

    @MutationMapping
    public Persona registrarPersonaConBienes(@Argument RegistroPersonaConBienesInput input) {
        return personaService.registrarPersonaConBienes(input);
    }
}
