package com.dev.ultron.controller;

import com.dev.ultron.config.GraphQlScalarConfig;
import com.dev.ultron.controller.personas.ClienteGraphQLController;
import com.dev.ultron.dto.personas.output.ClienteOutput;
import com.dev.ultron.dto.personas.output.PersonaOutput;
import com.dev.ultron.service.personas.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Test de slice GraphQL del controller de Clientes.
 * No requiere base de datos: el servicio se mockea con Mockito.
 */
@GraphQlTest({ClienteGraphQLController.class, HealthGraphQLController.class})
@Import(GraphQlScalarConfig.class)
public class ClienteGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void listarClientesDevuelveDatosMapeados() {
        ClienteOutput clienteMock = ClienteOutput.builder()
                .id_cliente(1L)
                .ruc("80012345-6")
                .tipoCliente("Empresa")
                .persona(PersonaOutput.builder()
                        .nombre("TEST")
                        .apellido("USER")
                        .documento("12345678")
                        .direccion("Av. Siempre Viva 123")
                        .build())
                .build();

        when(clienteService.listarTodosClientes()).thenReturn(List.of(clienteMock));

        String query = """
                query {
                    listarClientes {
                        id_cliente
                        ruc
                        tipoCliente
                        persona {
                            nombre
                            apellido
                            documento
                            direccion
                        }
                    }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("listarClientes[0].ruc").entity(String.class).isEqualTo("80012345-6")
                .path("listarClientes[0].persona.nombre").entity(String.class).isEqualTo("TEST")
                .path("listarClientes[0].persona.direccion").entity(String.class)
                .isEqualTo("Av. Siempre Viva 123");
    }
}
