package com.dev.ultron.dto.web.input;

import lombok.Data;

@Data
public class PagoTarjetaInput {
    private String titular;
    private String numero;
    private String vencimiento;
    private String cvc;
}
