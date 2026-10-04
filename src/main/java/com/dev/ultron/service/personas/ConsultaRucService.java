package com.dev.ultron.service.personas;

import com.dev.ultron.dto.personas.output.ContribuyenteRucOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.regex.Pattern;

/**
 * Consulta los datos públicos de un contribuyente (DNIT) en la API de TuRuc
 * para autocompletar el registro de clientes.
 */
@Service
public class ConsultaRucService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaRucService.class);

    private static final Pattern FORMATO_RUC = Pattern.compile("^\\d{1,8}(-\\d)?$");
    static final String MENSAJE_NO_DISPONIBLE =
            "No se pudo consultar el RUC en este momento. Cargá los datos a mano.";

    private final RestClient rucRestClient;

    public ConsultaRucService(@Qualifier("rucRestClient") RestClient rucRestClient) {
        this.rucRestClient = rucRestClient;
    }

    /**
     * @param ruc RUC con o sin dígito verificador, o número de cédula.
     * @return los datos del contribuyente, o {@code null} si no figura en la DNIT.
     */
    public ContribuyenteRucOutput consultar(String ruc) {
        String normalizado = normalizar(ruc);
        if (!FORMATO_RUC.matcher(normalizado).matches()) {
            throw new IllegalArgumentException(
                    "El RUC debe tener hasta 8 dígitos, con o sin dígito verificador (ej. 80012345-6)");
        }

        try {
            return rucRestClient.get()
                    .uri("/contribuyente/{ruc}", normalizado)
                    .exchange((request, response) -> {
                        int status = response.getStatusCode().value();
                        if (status == 400 || status == 404) {
                            return null;
                        }
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            log.warn("La API de RUC respondió {} para {}", status, normalizado);
                            throw new IllegalStateException(MENSAJE_NO_DISPONIBLE);
                        }
                        RespuestaTuRuc body = response.bodyTo(RespuestaTuRuc.class);
                        return body == null || body.data() == null ? null : mapear(body.data());
                    });
        } catch (RestClientException ex) {
            log.warn("No se pudo consultar el RUC {}: {}", normalizado, ex.getMessage());
            throw new IllegalStateException(MENSAJE_NO_DISPONIBLE, ex);
        }
    }

    private static String normalizar(String ruc) {
        return ruc == null ? "" : ruc.replaceAll("[\\s.]", "");
    }

    private static ContribuyenteRucOutput mapear(ContribuyenteTuRuc c) {
        String razonSocial = c.razonSocial() == null ? "" : c.razonSocial().trim();
        boolean juridica = Boolean.TRUE.equals(c.esPersonaJuridica());
        String estado = c.estado() == null ? null : c.estado().trim().toUpperCase();
        return ContribuyenteRucOutput.builder()
                .ruc(c.ruc())
                .documento(c.doc() == null ? null : String.valueOf(c.doc()))
                .dv(c.dv())
                .razonSocial(razonSocial)
                .nombre(juridica ? razonSocial : nombrePersonaFisica(razonSocial))
                .estado(estado)
                .activo("ACTIVO".equals(estado))
                .personaJuridica(juridica)
                .entidadPublica(Boolean.TRUE.equals(c.esEntidadPublica()))
                .build();
    }

    /** La DNIT publica a las personas físicas como "APELLIDOS, NOMBRES". */
    static String nombrePersonaFisica(String razonSocial) {
        int coma = razonSocial.indexOf(',');
        if (coma < 0) {
            return razonSocial;
        }
        String apellidos = razonSocial.substring(0, coma).trim();
        String nombres = razonSocial.substring(coma + 1).trim();
        if (nombres.isEmpty()) {
            return apellidos;
        }
        return (nombres + " " + apellidos).trim();
    }

    record RespuestaTuRuc(ContribuyenteTuRuc data, String message) {
    }

    record ContribuyenteTuRuc(
            Long doc,
            String razonSocial,
            Integer dv,
            String ruc,
            String estado,
            Boolean esPersonaJuridica,
            Boolean esEntidadPublica) {
    }
}
