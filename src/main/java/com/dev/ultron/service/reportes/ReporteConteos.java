package com.dev.ultron.service.reportes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ReporteConteos {

    private ReporteConteos() {
    }

    static Map<Long, Integer> deFilas(List<Object[]> filas) {
        Map<Long, Integer> conteos = new HashMap<>();
        if (filas == null) {
            return conteos;
        }
        for (Object[] fila : filas) {
            if (fila == null || fila.length < 2 || fila[0] == null) {
                continue;
            }
            long id = ((Number) fila[0]).longValue();
            int cantidad = fila[1] == null ? 0 : ((Number) fila[1]).intValue();
            conteos.put(id, cantidad);
        }
        return conteos;
    }

    static List<Long> ids(Collection<Long> ids) {
        return ids.stream().filter(id -> id != null).distinct().toList();
    }
}
