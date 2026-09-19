package com.dev.ultron.dto.reportes.mapper;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.domain.taller.OrdenDiagnostico;
import com.dev.ultron.domain.taller.OrdenDiagnosticoHallazgo;
import com.dev.ultron.domain.taller.OrdenEstadoVehiculo;
import com.dev.ultron.domain.taller.OrdenRecepcion;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import com.dev.ultron.dto.reportes.output.ReporteOtDetalleFila;
import com.dev.ultron.utilitarios.AppConstants;
import com.dev.ultron.utilitarios.DateUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ReporteOtDetalleMapper {

    private static final Locale LOCALE_PY = Locale.of("es", "PY");

    private ReporteOtDetalleMapper() {
    }

    public static List<ReporteOtDetalleFila> deOrden(OrdenTrabajo orden) {
        String encabezado = construirEncabezado(orden);
        String presupuesto = formatGs(orden.getDiagnostico() != null
                ? orden.getDiagnostico().getTotalPresupuesto()
                : null);
        String fecha = nvl(DateUtil.format(orden.getFechaCreacion()));
        String etapa = nvl(orden.getEtapa());
        String numero = nvl(orden.getNumeroOrden());

        List<OrdenTrabajoDetalle> detalles = orden.getDetalles() == null
                ? List.of()
                : orden.getDetalles();
        if (detalles.isEmpty()) {
            return List.of(base(numero, etapa, fecha, presupuesto, encabezado)
                    .tipoLinea("")
                    .nombreLinea("Sin productos ni servicios cargados")
                    .cantidad("")
                    .precio("")
                    .subtotal("")
                    .build());
        }

        List<ReporteOtDetalleFila> filas = new ArrayList<>(detalles.size());
        for (OrdenTrabajoDetalle detalle : detalles) {
            filas.add(base(numero, etapa, fecha, presupuesto, encabezado)
                    .tipoLinea(nvl(detalle.getTipo()))
                    .nombreLinea(nombreLinea(detalle))
                    .cantidad(formatCantidad(detalle.getCantidad()))
                    .precio(formatGs(detalle.getPrecioUnitario()))
                    .subtotal(formatGs(detalle.getSubtotal()))
                    .build());
        }
        return filas;
    }

    private static ReporteOtDetalleFila.ReporteOtDetalleFilaBuilder base(
            String numero,
            String etapa,
            String fecha,
            String presupuesto,
            String encabezado
    ) {
        return ReporteOtDetalleFila.builder()
                .numeroOrden(numero)
                .etapa(etapa)
                .fecha(fecha)
                .presupuesto(presupuesto)
                .encabezado(encabezado);
    }

    private static String construirEncabezado(OrdenTrabajo orden) {
        Persona cliente = orden.getCliente() != null ? orden.getCliente().getPersona() : null;
        Vehiculo vehiculo = orden.getVehiculo();
        OrdenRecepcion recepcion = orden.getRecepcion();
        OrdenDiagnostico diagnostico = orden.getDiagnostico();
        Usuario responsable = orden.getResponsable();

        StringBuilder sb = new StringBuilder();
        linea(sb, "Cliente", nombreCompleto(cliente));
        linea(sb, "Documento", cliente != null ? nvl(cliente.getDocumento()) : "");
        linea(sb, "Vehículo", vehiculoTexto(vehiculo));
        linea(sb, "Chapa", vehiculo != null ? nvl(vehiculo.getChapa()) : "");
        linea(sb, "Sector", orden.getSector() != null ? nvl(orden.getSector().getNombre()) : "");
        linea(sb, "Mecánicos", nombresMecanicos(orden));
        linea(sb, "Responsable", responsable != null ? nvl(responsable.getUsername()) : "");
        linea(sb, "Falla", recepcion != null ? nvl(recepcion.getDescripcionFalla()) : "");
        if (diagnostico != null) {
            linea(sb, "Inicio estimado", nvl(DateUtil.format(diagnostico.getFechaInicioEstimada())));
            linea(sb, "Fin estimado", nvl(DateUtil.format(diagnostico.getFechaFinEstimada())));
            linea(sb, "Presupuesto", formatGs(diagnostico.getTotalPresupuesto())
                    + (diagnostico.isPresupuestoAprobado() ? " (aprobado)" : ""));
            linea(sb, "Observaciones", nvl(diagnostico.getObservaciones()));
        }
        linea(sb, "Estado del vehículo", estadoVehiculo(orden.getEstadoVehiculo()));
        linea(sb, "Hallazgos", hallazgos(orden.getHallazgos()));
        return sb.toString().strip();
    }

    private static String vehiculoTexto(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return "";
        }
        return join(" ", vehiculo.getMarca(), vehiculo.getModelo(),
                vehiculo.getAnio() != null ? String.valueOf(vehiculo.getAnio()) : null);
    }

    private static String estadoVehiculo(OrdenEstadoVehiculo estado) {
        if (estado == null) {
            return "";
        }
        List<String> marcas = new ArrayList<>();
        addFlag(marcas, estado.isFallaMecanica(), "Falla mecánica");
        addFlag(marcas, estado.isFallaElectrica(), "Falla eléctrica");
        addFlag(marcas, estado.isEstadoLlantas(), "Llantas");
        addFlag(marcas, estado.isEstadoPintura(), "Pintura");
        addFlag(marcas, estado.isEstadoRayones(), "Rayones");
        addFlag(marcas, estado.isEstadoGolpes(), "Golpes");
        addFlag(marcas, estado.isEstadoVidrios(), "Vidrios");
        addFlag(marcas, estado.isPerdidaAceite(), "Pérdida de aceite");
        addFlag(marcas, estado.isLucesDanadas(), "Luces dañadas");
        addFlag(marcas, estado.isEspejosDanados(), "Espejos dañados");
        addFlag(marcas, estado.isAccesoriosFaltantes(), "Accesorios faltantes");
        if (estado.getKilometraje() != null) {
            marcas.add("Km " + estado.getKilometraje());
        }
        if (estado.getNivelCombustible() != null && !estado.getNivelCombustible().isBlank()) {
            marcas.add("Combustible " + estado.getNivelCombustible());
        }
        if (estado.getObservacionesEstado() != null && !estado.getObservacionesEstado().isBlank()) {
            marcas.add(estado.getObservacionesEstado().trim());
        }
        return String.join(" · ", marcas);
    }

    private static String hallazgos(List<OrdenDiagnosticoHallazgo> hallazgos) {
        if (hallazgos == null || hallazgos.isEmpty()) {
            return "";
        }
        List<String> lineas = new ArrayList<>();
        for (OrdenDiagnosticoHallazgo hallazgo : hallazgos) {
            String prefijo = join(" / ", hallazgo.getTipo(), hallazgo.getGravedad(), hallazgo.getSistema());
            String desc = nvl(hallazgo.getDescripcion());
            lineas.add(prefijo.isBlank() ? desc : prefijo + " — " + desc);
        }
        return String.join(" | ", lineas);
    }

    private static String nombresMecanicos(OrdenTrabajo orden) {
        List<String> nombres = new ArrayList<>();
        if (orden.getMecanicos() != null) {
            for (var funcionario : orden.getMecanicos()) {
                String nombre = nombreCompleto(funcionario != null ? funcionario.getPersona() : null);
                if (!nombre.isBlank() && !nombres.contains(nombre)) {
                    nombres.add(nombre);
                }
            }
        }
        if (nombres.isEmpty() && orden.getMecanico() != null) {
            String nombre = nombreCompleto(orden.getMecanico().getPersona());
            if (!nombre.isBlank()) {
                nombres.add(nombre);
            }
        }
        return String.join(", ", nombres);
    }

    private static String nombreLinea(OrdenTrabajoDetalle detalle) {
        Producto producto = detalle.getProducto();
        if (producto != null && producto.getNombre() != null && !producto.getNombre().isBlank()) {
            return producto.getNombre();
        }
        Servicio servicio = detalle.getServicio();
        if (servicio != null && servicio.getNombre() != null && !servicio.getNombre().isBlank()) {
            String mecanico = detalle.getMecanico() != null
                    ? nombreCompleto(detalle.getMecanico().getPersona())
                    : "";
            return mecanico.isBlank() ? servicio.getNombre() : servicio.getNombre() + " — " + mecanico;
        }
        return nvl(detalle.getDescripcion());
    }

    private static void addFlag(List<String> marcas, boolean activo, String label) {
        if (activo) {
            marcas.add(label);
        }
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

    private static String formatGs(BigDecimal value) {
        return formatCantidad(value) + " " + AppConstants.MONEDA_DEFAULT;
    }

    private static String formatCantidad(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        NumberFormat nf = NumberFormat.getInstance(LOCALE_PY);
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        return nf.format(value);
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
