package com.dev.ultron.service.personas;

import com.dev.ultron.dto.personas.output.ContribuyenteRucOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ConsultaRucServiceTest {

    private static final String BASE = "https://ruc.test/api";

    private MockRestServiceServer server;
    private ConsultaRucService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        service = new ConsultaRucService(builder.build());
    }

    @Test
    void personaFisicaReordenaElNombre() {
        server.expect(requestTo(BASE + "/contribuyente/11584"))
                .andRespond(withSuccess("""
                        {"data":{"doc":11584,"razonSocial":"JARA GONZALEZ, FEDERICO","dv":3,
                        "ruc":"11584-3","estado":"ACTIVO","esPersonaJuridica":false,
                        "esEntidadPublica":false},"message":"OK"}
                        """, MediaType.APPLICATION_JSON));

        ContribuyenteRucOutput resultado = service.consultar(" 11.584 ");

        assertThat(resultado.ruc()).isEqualTo("11584-3");
        assertThat(resultado.documento()).isEqualTo("11584");
        assertThat(resultado.nombre()).isEqualTo("FEDERICO JARA GONZALEZ");
        assertThat(resultado.razonSocial()).isEqualTo("JARA GONZALEZ, FEDERICO");
        assertThat(resultado.activo()).isTrue();
        assertThat(resultado.personaJuridica()).isFalse();
        server.verify();
    }

    @Test
    void personaJuridicaMantieneLaRazonSocialYMarcaElEstado() {
        server.expect(requestTo(BASE + "/contribuyente/80012345-6"))
                .andRespond(withSuccess("""
                        {"data":{"doc":80012345,"razonSocial":"COMERCIO Y FINANZAS SA","dv":0,
                        "ruc":"80012345-0","estado":"BLOQUEADO","esPersonaJuridica":true,
                        "esEntidadPublica":false},"message":"OK"}
                        """, MediaType.APPLICATION_JSON));

        ContribuyenteRucOutput resultado = service.consultar("80012345-6");

        assertThat(resultado.nombre()).isEqualTo("COMERCIO Y FINANZAS SA");
        assertThat(resultado.estado()).isEqualTo("BLOQUEADO");
        assertThat(resultado.activo()).isFalse();
        assertThat(resultado.personaJuridica()).isTrue();
    }

    @Test
    void rucInexistenteDevuelveNull() {
        server.expect(requestTo(BASE + "/contribuyente/1234567"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"data\":null,\"message\":\"No se encontraron registros\"}"));

        assertThat(service.consultar("1234567")).isNull();
    }

    @Test
    void servicioCaidoDaUnMensajeClaro() {
        server.expect(requestTo(BASE + "/contribuyente/1234567"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> service.consultar("1234567"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(ConsultaRucService.MENSAJE_NO_DISPONIBLE);
    }

    @Test
    void formatoInvalidoNoLlamaALaApi() {
        assertThatThrownBy(() -> service.consultar("ABC-12"))
                .isInstanceOf(IllegalArgumentException.class);
        server.verify();
    }

    @Test
    void nombrePersonaFisicaSinComaQuedaIgual() {
        assertThat(ConsultaRucService.nombrePersonaFisica("JUAN PEREZ")).isEqualTo("JUAN PEREZ");
    }
}
