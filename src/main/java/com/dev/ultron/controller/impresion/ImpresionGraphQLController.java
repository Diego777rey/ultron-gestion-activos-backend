package com.dev.ultron.controller.impresion;

import com.dev.ultron.dto.impresion.input.TicketVentaInput;
import com.dev.ultron.dto.impresion.output.ImpresionResultado;
import com.dev.ultron.dto.impresion.output.ImpresoraOutput;
import com.dev.ultron.service.impresion.ImpresionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ImpresionGraphQLController {

    private final ImpresionService service;

    @QueryMapping
    public List<ImpresoraOutput> listarImpresoras() {
        return service.listarImpresoras();
    }

    @MutationMapping
    public ImpresionResultado imprimirPrueba(@Argument String printerName) {
        return service.imprimirPrueba(printerName);
    }

    @MutationMapping
    public ImpresionResultado imprimirTicketVenta(
            @Argument String printerName,
            @Argument TicketVentaInput ticket) {
        return service.imprimirTicketVenta(printerName, ticket);
    }
}
