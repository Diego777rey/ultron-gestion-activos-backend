package com.dev.ultron.dto.reportes.mapper;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.operaciones.Transferencia;
import com.dev.ultron.domain.operaciones.TransferenciaDetalle;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.reportes.output.ReporteTransferenciaDetalleFila;
import com.dev.ultron.utilitarios.DateUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ReporteTransferenciaDetalleMapper {

    private static final Locale LOCALE_PY = Locale.of("es", "PY");

    private ReporteTransferenciaDetalleMapper() {
    }

    public static List<ReporteTransferenciaDetalleFila> deTransferencia(Transferencia transferencia) {
        String encabezado = construirEncabezado(transferencia);
        String numero = nvl(transferencia.getNumero());
        String estado = estadoCabecera(transferencia.getEstado());
        String fecha = nvl(DateUtil.format(transferencia.getFecha()));
        String firmaEntrega = nombreCompleto(transferencia.getPersona());
        String firmaRecepcion = nombreCompleto(transferencia.getPersonaRecepcion());

        List<TransferenciaDetalle> detalles = transferencia.getDetalles() == null
                ? List.of()
                : transferencia.getDetalles();
        if (detalles.isEmpty()) {
            return List.of(base(numero, estado, fecha, encabezado, firmaEntrega, firmaRecepcion)
                    .codigo("")
                    .nombreLinea("Sin productos cargados")
                    .cantidad("")
                    .estadoLinea("")
                    .build());
        }

        List<ReporteTransferenciaDetalleFila> filas = new ArrayList<>(detalles.size());
        for (TransferenciaDetalle detalle : detalles) {
            Producto producto = detalle.getProducto();
            filas.add(base(numero, estado, fecha, encabezado, firmaEntrega, firmaRecepcion)
                    .codigo(producto != null ? nvl(producto.getCodigo()) : "")
                    .nombreLinea(nombreProducto(producto))
                    .cantidad(formatCantidad(detalle.getCantidad()))
                    .estadoLinea(estadoLinea(detalle))
                    .build());
        }
        return filas;
    }

    private static ReporteTransferenciaDetalleFila.ReporteTransferenciaDetalleFilaBuilder base(
            String numero,
            String estado,
            String fecha,
            String encabezado,
            String firmaEntrega,
            String firmaRecepcion
    ) {
        return ReporteTransferenciaDetalleFila.builder()
                .numero(numero)
                .estado(estado)
                .fecha(fecha)
                .encabezado(encabezado)
                .firmaEntrega(firmaEntrega)
                .firmaRecepcion(firmaRecepcion);
    }

    private static String construirEncabezado(Transferencia transferencia) {
        StringBuilder sb = new StringBuilder();
        linea(sb, "Sector origen", transferencia.getSectorOrigen() != null
                ? nvl(transferencia.getSectorOrigen().getNombre()) : "");
        linea(sb, "Sector destino", transferencia.getSectorDestino() != null
                ? nvl(transferencia.getSectorDestino().getNombre()) : "");
        linea(sb, "Entregado por", nombreCompleto(transferencia.getPersona()));
        linea(sb, "Recibido por", nombreCompleto(transferencia.getPersonaRecepcion()));
        linea(sb, "Observación", nvl(transferencia.getObservacion()));
        return sb.toString().strip();
    }

    private static String nombreProducto(Producto producto) {
        if (producto == null) {
            return "";
        }
        return nvl(producto.getNombre());
    }

    private static String estadoCabecera(String estado) {
        return switch (nvl(estado).toUpperCase(Locale.ROOT)) {
            case "CREACION", "PENDIENTE" -> "Creación";
            case "PENDIENTE_CONFERIR" -> "Pendiente a conferir";
            case "CONFERIDO" -> "Conferido";
            case "RECEPCIONADO" -> "Recepcionado";
            default -> nvl(estado);
        };
    }

    private static String estadoLinea(TransferenciaDetalle detalle) {
        String estado = nvl(detalle.getEstado()).toUpperCase(Locale.ROOT);
        String label = switch (estado) {
            case "VERIFICADO" -> "Verificado";
            case "RECHAZADO" -> "Rechazado";
            case "PENDIENTE" -> "Pendiente";
            default -> nvl(detalle.getEstado());
        };
        if ("RECHAZADO".equals(estado) && detalle.getMotivoRechazo() != null && !detalle.getMotivoRechazo().isBlank()) {
            String extra = detalle.getMotivoRechazoDetalle() != null && !detalle.getMotivoRechazoDetalle().isBlank()
                    ? detalle.getMotivoRechazo() + ": " + detalle.getMotivoRechazoDetalle()
                    : detalle.getMotivoRechazo();
            return label + " (" + extra + ")";
        }
        return label;
    }

    private static void linea(StringBuilder sb, String etiqueta, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append('\n');
        }
        sb.append(etiqueta).append(": ").append(valor.trim());
    }

    private static String nombreCompleto(Persona persona) {
        if (persona == null) {
            return "";
        }
        return join(" ", persona.getNombre(), persona.getApellido());
    }

    private static String join(String separator, String... parts) {
        StringBuilder builder = new StringBuilder();
        if (parts == null) {
            return "";
        }
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(part.trim());
        }
        return builder.toString();
    }

    private static String formatCantidad(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        NumberFormat nf = NumberFormat.getInstance(LOCALE_PY);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);
        return nf.format(value);
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
