package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.output.VueltoOutput;
import com.dev.ultron.service.financiero.VueltoService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class VueltoGraphQLController {

    private final VueltoService service;

    @QueryMapping
    public VueltoOutput calcularVuelto(@Argument BigDecimal totalPyg,
                                       @Argument BigDecimal montoRecibido,
                                       @Argument String monedaRecibida,
                                       @Argument String monedaVuelto) {
        return service.calcularVuelto(totalPyg, montoRecibido, monedaRecibida, monedaVuelto);
    }
}
