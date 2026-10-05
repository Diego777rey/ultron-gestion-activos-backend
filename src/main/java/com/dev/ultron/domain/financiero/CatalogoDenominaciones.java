package com.dev.ultron.domain.financiero;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.dev.ultron.domain.financiero.Denominacion.billete;
import static com.dev.ultron.domain.financiero.Denominacion.moneda;

/**
 * Billetes y monedas en circulación por divisa, usados para desglosar el vuelto.
 * Cada lista está ordenada de mayor a menor (orden que recorre el algoritmo voraz).
 * Las divisas sin catálogo se pueden cobrar y devolver, pero sin desglose.
 */
public final class CatalogoDenominaciones {

    public static final String PYG = "PYG";

    /** Guaraní: sin centavos. */
    private static final List<Denominacion> GUARANIES = List.of(
            billete("100000"), billete("50000"), billete("20000"), billete("10000"),
            billete("5000"), billete("2000"),
            moneda("1000"), moneda("500"), moneda("100"), moneda("50"));

    /** Dólar estadounidense. */
    private static final List<Denominacion> DOLARES = List.of(
            billete("100"), billete("50"), billete("20"), billete("10"),
            billete("5"), billete("2"), billete("1"),
            moneda("0.25"), moneda("0.10"), moneda("0.05"), moneda("0.01"));

    /** Real brasileño. */
    private static final List<Denominacion> REALES = List.of(
            billete("200"), billete("100"), billete("50"), billete("20"),
            billete("10"), billete("5"), billete("2"),
            moneda("1"), moneda("0.50"), moneda("0.25"), moneda("0.10"), moneda("0.05"));

    private static final Map<String, List<Denominacion>> POR_MONEDA = Map.of(
            "PYG", GUARANIES,
            "USD", DOLARES,
            "BRL", REALES);

    /** Nombres libres que usan las cotizaciones, mapeados a su código ISO. */
    private static final Map<String, String> ALIAS = Map.ofEntries(
            Map.entry("pyg", "PYG"), Map.entry("gs", "PYG"), Map.entry("guarani", "PYG"), Map.entry("guaranies", "PYG"),
            Map.entry("usd", "USD"), Map.entry("dolar", "USD"), Map.entry("dolares", "USD"),
            Map.entry("brl", "BRL"), Map.entry("real", "BRL"), Map.entry("reales", "BRL"), Map.entry("real brasileno", "BRL"));

    private CatalogoDenominaciones() {
    }

    /** Código ISO de la divisa a partir de su nombre libre ("Dólar" → USD). Desconocidos: el texto en mayúsculas. */
    public static String codigo(String moneda) {
        if (moneda == null || moneda.isBlank()) {
            return PYG;
        }
        String clave = java.text.Normalizer.normalize(moneda.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return ALIAS.getOrDefault(clave, moneda.trim().toUpperCase(Locale.ROOT));
    }

    public static boolean esGuarani(String moneda) {
        return PYG.equals(codigo(moneda));
    }

    /** Decimales con los que opera la divisa: 0 para guaraníes, 2 para el resto. */
    public static int escala(String moneda) {
        return esGuarani(moneda) ? 0 : 2;
    }

    /** Denominaciones de mayor a menor, o vacío si la divisa no tiene catálogo. */
    public static Optional<List<Denominacion>> de(String moneda) {
        List<Denominacion> lista = POR_MONEDA.get(codigo(moneda));
        if (lista == null) {
            return Optional.empty();
        }
        return Optional.of(lista.stream()
                .sorted(Comparator.comparing(Denominacion::valor).reversed())
                .toList());
    }
}
