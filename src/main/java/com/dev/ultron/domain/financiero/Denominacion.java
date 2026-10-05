package com.dev.ultron.domain.financiero;

import java.math.BigDecimal;

/** Un billete o moneda de una divisa, con su valor nominal. */
public record Denominacion(BigDecimal valor, Tipo tipo) {

    public enum Tipo { BILLETE, MONEDA }

    public static Denominacion billete(String valor) {
        return new Denominacion(new BigDecimal(valor), Tipo.BILLETE);
    }

    public static Denominacion moneda(String valor) {
        return new Denominacion(new BigDecimal(valor), Tipo.MONEDA);
    }

    /** Valor expresado en la unidad mínima de la divisa (guaraníes o centavos), según {@code escala}. */
    public long enMinimos(int escala) {
        return valor.movePointRight(escala).longValueExact();
    }
}
