package com.dev.ultron.controller.web;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import com.dev.ultron.dto.web.input.LoginClienteWebInput;
import com.dev.ultron.dto.web.input.PedidoWebInput;
import com.dev.ultron.dto.web.input.RegistroClienteWebInput;
import com.dev.ultron.dto.web.input.SuscripcionNewsletterInput;
import com.dev.ultron.dto.web.output.PedidoWebOutput;
import com.dev.ultron.dto.web.output.SesionClienteWebOutput;
import com.dev.ultron.dto.web.output.SuscripcionNewsletterOutput;
import com.dev.ultron.service.web.CuentaClienteWebService;
import com.dev.ultron.service.web.PedidoWebService;
import com.dev.ultron.service.web.SuscripcionNewsletterService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ComercioWebGraphQLController {

    private final PedidoWebService pedidoWebService;
    private final SuscripcionNewsletterService suscripcionNewsletterService;
    private final CuentaClienteWebService cuentaClienteWebService;

    @QueryMapping
    public PedidoWebOutput buscarPedidoWebPorCodigo(@Argument String codigo) {
        return pedidoWebService.buscarPorCodigo(codigo);
    }

    @QueryMapping
    public SesionClienteWebOutput miCuentaClienteWeb(@AuthenticationPrincipal Jwt jwt) {
        return cuentaClienteWebService.miCuenta(jwt);
    }

    @MutationMapping
    public PedidoWebOutput registrarPedidoWeb(@Argument PedidoWebInput input) {
        return pedidoWebService.registrarPedido(input);
    }

    @MutationMapping
    public SuscripcionNewsletterOutput suscribirNewsletter(@Argument SuscripcionNewsletterInput input) {
        return suscripcionNewsletterService.suscribir(input);
    }

    @MutationMapping
    public SesionClienteWebOutput registrarCuentaClienteWeb(@Argument RegistroClienteWebInput input) {
        return cuentaClienteWebService.registrar(input);
    }

    @MutationMapping
    public SesionClienteWebOutput loginClienteWeb(@Argument LoginClienteWebInput input) {
        return cuentaClienteWebService.login(input);
    }
}
