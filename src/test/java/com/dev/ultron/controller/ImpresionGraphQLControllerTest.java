package com.dev.ultron.controller;

import com.dev.ultron.config.GraphQlScalarConfig;
import com.dev.ultron.controller.impresion.ImpresionGraphQLController;
import com.dev.ultron.dto.impresion.output.ImpresionResultado;
import com.dev.ultron.dto.impresion.output.ImpresoraOutput;
import com.dev.ultron.service.impresion.ImpresionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@GraphQlTest({ImpresionGraphQLController.class, HealthGraphQLController.class})
@Import(GraphQlScalarConfig.class)
class ImpresionGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private ImpresionService impresionService;

    @Test
    void listarImpresorasDevuelveColasDelSistema() {
        when(impresionService.listarImpresoras()).thenReturn(List.of(
                ImpresoraOutput.builder()
                        .name("TICKET58")
                        .displayName("TICKET58")
                        .isDefault(true)
                        .build()
        ));

        graphQlTester.document("""
                        query {
                            listarImpresoras {
                                name
                                displayName
                                isDefault
                            }
                        }
                        """)
                .execute()
                .path("listarImpresoras[0].name").entity(String.class).isEqualTo("TICKET58")
                .path("listarImpresoras[0].isDefault").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    void imprimirPruebaDelegaEnElServicio() {
        when(impresionService.imprimirPrueba("TICKET58"))
                .thenReturn(ImpresionResultado.ok("Ticket de prueba enviado (TICKET58)"));

        graphQlTester.document("""
                        mutation {
                            imprimirPrueba(printerName: "TICKET58") {
                                success
                                message
                            }
                        }
                        """)
                .execute()
                .path("imprimirPrueba.success").entity(Boolean.class).isEqualTo(true)
                .path("imprimirPrueba.message").entity(String.class)
                .isEqualTo("Ticket de prueba enviado (TICKET58)");
    }

    @Test
    void imprimirTicketVentaEnviaPayloadGenerico() {
        when(impresionService.imprimirTicketVenta(eq("TICKET58"), any()))
                .thenReturn(ImpresionResultado.ok("Ticket enviado a la impresora (TICKET58)"));

        graphQlTester.document("""
                        mutation($ticket: TicketVentaInput!) {
                            imprimirTicketVenta(printerName: "TICKET58", ticket: $ticket) {
                                success
                                message
                            }
                        }
                        """)
                .variable("ticket", java.util.Map.of(
                        "titulo", "CH-SERVICE",
                        "numero", "VEN-1",
                        "total", 1000,
                        "lineas", List.of(java.util.Map.of(
                                "descripcion", "Producto",
                                "cantidad", 1,
                                "precioUnitario", 1000,
                                "subtotal", 1000
                        ))
                ))
                .execute()
                .path("imprimirTicketVenta.success").entity(Boolean.class).isEqualTo(true);
    }
}
