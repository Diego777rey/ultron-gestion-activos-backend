package com.dev.ultron.controller;

import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.RegistroPersonaConBienesInput;
import com.dev.ultron.service.PersonaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(PersonaGraphQLController.class)
public class PersonaGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private PersonaService personaService;

    @Test
    void registrarPersonaConBienes() {
        // Mock result
        Persona personaMock = new Persona();
        personaMock.setId_persona(1L);
        personaMock.setNombre("Test");
        personaMock.setApellido("User");

        when(personaService.registrarPersonaConBienes(any(RegistroPersonaConBienesInput.class)))
                .thenReturn(personaMock);

        String mutation = """
                mutation {
                    registrarPersonaConBienes(input: {
                        persona: {
                            nombre: "Test",
                            apellido: "User",
                            documento: "12345678",
                            email: "test@example.com",
                            telefono: "555-0101",
                            estado: "ACTIVO"
                        },
                        bienes: [
                            {
                                bien: {
                                    tipo: "MUEBLE",
                                    descripcion: "Silla",
                                    valor: 100.00,
                                    estado: "NUEVO"
                                },
                                porcentaje: 100.00
                            }
                        ]
                    }) {
                        id_persona
                        nombre
                        apellido
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .path("registrarPersonaConBienes.nombre").entity(String.class).isEqualTo("Test")
                .path("registrarPersonaConBienes.apellido").entity(String.class).isEqualTo("User");
    }
}
