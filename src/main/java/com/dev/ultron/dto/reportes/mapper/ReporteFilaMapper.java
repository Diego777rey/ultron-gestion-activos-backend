package com.dev.ultron.dto.reportes.mapper;

import com.dev.ultron.domain.inventario.CategoriaProducto;
import com.dev.ultron.domain.inventario.CategoriaServicio;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.operaciones.Transferencia;
import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Funcionario;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.domain.personas.UsuarioRole;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.SolicitudRepuesto;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.utilitarios.AppConstants;
import com.dev.ultron.utilitarios.DateUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Convierte entidades de inventario en filas del reporte genérico.
 */
public final class ReporteFilaMapper {

    private static final Locale LOCALE_PY = Locale.of("es", "PY");

    private ReporteFilaMapper() {
    }

    public static ReporteFila deProducto(Producto producto, int numero) {
        String[] categorias = resolverCategoriaProducto(producto.getCategoriaProducto());
        String codigo = firstNonBlank(producto.getCodigoBarras(), producto.getCodigo());
        return ReporteFila.builder()
                .numero(numero)
                .codigo(nvl(codigo))
                .nombre(nvl(producto.getNombre()))
                .descripcion(nvl(producto.getDescripcion()))
                .categoria(categorias[0])
                .subcategoria(categorias[1])
                .precio(formatGs(producto.getPrecioVenta()))
                .extra(formatCantidad(producto.getStock()))
                .estado(estado(producto.isEstado()))
                .build();
    }

    public static ReporteFila deServicio(Servicio servicio, int numero) {
        String[] categorias = resolverCategoriaServicio(servicio.getCategoriaServicio());
        return ReporteFila.builder()
                .numero(numero)
                .codigo(nvl(servicio.getCodigo()))
                .nombre(nvl(servicio.getNombre()))
                .descripcion(nvl(servicio.getDescripcion()))
                .categoria(categorias[0])
                .subcategoria(categorias[1])
                .precio(formatGs(servicio.getPrecio()))
                .extra("")
                .estado(estado(servicio.isEstado()))
                .build();
    }

    public static ReporteFila deCliente(Cliente cliente, int numero) {
        Persona persona = cliente.getPersona();
        return ReporteFila.builder()
                .numero(numero)
                .codigo(firstNonBlank(persona != null ? persona.getDocumento() : null, cliente.getRuc()))
                .nombre(nombreCompleto(persona))
                .descripcion(nvl(cliente.getObservaciones()))
                .categoria(nvl(cliente.getTipoCliente()))
                .subcategoria(nvl(cliente.getRuc()))
                .precio(formatGs(cliente.getLimiteCredito()))
                .extra(persona != null ? nvl(persona.getTelefono()) : "")
                .estado(estado(cliente.isEstado()))
                .build();
    }

    public static ReporteFila deVehiculo(Vehiculo vehiculo, int numero) {
        Cliente cliente = vehiculo.getCliente();
        Persona persona = cliente != null ? cliente.getPersona() : null;
        return ReporteFila.builder()
                .numero(numero)
                .codigo(nvl(vehiculo.getChapa()))
                .nombre(join(" ", vehiculo.getMarca(), vehiculo.getModelo()))
                .descripcion(nvl(vehiculo.getDescripcion()))
                .categoria(nvl(vehiculo.getTipoVehiculo()))
                .subcategoria(vehiculo.getAnio() != null ? String.valueOf(vehiculo.getAnio()) : "")
                .precio(formatGs(vehiculo.getValor()))
                .extra(firstNonBlank(nombreCompleto(persona), cliente != null ? cliente.getRuc() : null))
                .estado(nvl(vehiculo.getEstado()))
                .build();
    }

    public static ReporteFila deFuncionario(Funcionario funcionario, int numero) {
        Persona persona = funcionario.getPersona();
        return ReporteFila.builder()
                .numero(numero)
                .codigo(persona != null ? nvl(persona.getDocumento()) : "")
                .nombre(nombreCompleto(persona))
                .descripcion(join(" · ", persona != null ? persona.getEmail() : null,
                        persona != null ? persona.getTelefono() : null))
                .categoria(nvl(funcionario.getSector()))
                .subcategoria(persona != null ? nvl(persona.getDireccion()) : "")
                .precio(formatGs(funcionario.getSueldo()))
                .extra(nvl(DateUtil.format(funcionario.getFechaIngreso())))
                .estado(estado(funcionario.isEstado()))
                .build();
    }

    public static ReporteFila deUsuario(Usuario usuario, int numero) {
        Funcionario funcionario = usuario.getFuncionario();
        Persona persona = funcionario != null ? funcionario.getPersona() : null;
        String roles = usuario.getUsuarioRoles() == null ? "" : usuario.getUsuarioRoles().stream()
                .map(UsuarioRole::getRole)
                .filter(role -> role != null && role.getDescripcion() != null)
                .map(role -> role.getDescripcion())
                .distinct()
                .collect(Collectors.joining(", "));
        String primerRol = roles.contains(",") ? roles.substring(0, roles.indexOf(',')).trim() : roles;
        return ReporteFila.builder()
                .numero(numero)
                .codigo(nvl(usuario.getUsername()))
                .nombre(firstNonBlank(nombreCompleto(persona), usuario.getUsername()))
                .descripcion(nvl(usuario.getEmail()))
                .categoria(primerRol)
                .subcategoria(funcionario != null ? nvl(funcionario.getSector()) : "")
                .precio("")
                .extra(roles)
                .estado(Boolean.TRUE.equals(usuario.getActivo()) ? AppConstants.Estados.ACTIVO : AppConstants.Estados.INACTIVO)
                .build();
    }

    public static ReporteFila deTransferencia(Transferencia transferencia, int numero) {
        String origen = transferencia.getSectorOrigen() != null ? nvl(transferencia.getSectorOrigen().getNombre()) : "";
        String destino = transferencia.getSectorDestino() != null ? nvl(transferencia.getSectorDestino().getNombre()) : "";
        int items = transferencia.getDetalles() == null ? 0 : transferencia.getDetalles().size();
        return ReporteFila.builder()
                .numero(numero)
                .codigo(nvl(transferencia.getNumero()))
                .nombre(join(" → ", origen, destino))
                .descripcion(nvl(transferencia.getObservacion()))
                .categoria(origen)
                .subcategoria(destino)
                .precio(nvl(formatFechaHora(transferencia.getFecha())))
                .extra(String.valueOf(items))
                .estado(nvl(transferencia.getEstado()))
                .build();
    }

    public static ReporteFila deSolicitudRepuesto(SolicitudRepuesto solicitud, int numero) {
        String origen = solicitud.getSectorOrigen() != null ? nvl(solicitud.getSectorOrigen().getNombre()) : "";
        String destino = solicitud.getSectorDestino() != null ? nvl(solicitud.getSectorDestino().getNombre()) : "";
        String ot = solicitud.getOrdenTrabajo() != null ? nvl(solicitud.getOrdenTrabajo().getNumeroOrden()) : "";
        int items = solicitud.getDetalles() == null ? 0 : solicitud.getDetalles().size();
        String descripcion = firstNonBlank(solicitud.getObservacion(), solicitud.getMotivoRechazo());
        return ReporteFila.builder()
                .numero(numero)
                .codigo("SR-" + solicitud.getId_solicitud_repuesto())
                .nombre(ot)
                .descripcion(descripcion)
                .categoria(origen)
                .subcategoria(destino)
                .precio(nvl(formatFechaHora(solicitud.getFecha())))
                .extra(items + " · " + ot)
                .estado(nvl(solicitud.getEstado()))
                .build();
    }

    public static ReporteFila deOrdenTrabajo(OrdenTrabajo orden, int numero) {
        return deOrdenTrabajo(orden, numero, false);
    }

    public static ReporteFila deOrdenTrabajoHistorial(OrdenTrabajo orden, int numero) {
        return deOrdenTrabajo(orden, numero, true);
    }

    private static ReporteFila deOrdenTrabajo(OrdenTrabajo orden, int numero, boolean historial) {
        Persona clientePersona = orden.getCliente() != null ? orden.getCliente().getPersona() : null;
        String chapa = orden.getVehiculo() != null ? nvl(orden.getVehiculo().getChapa()) : "";
        String vehiculo = orden.getVehiculo() == null ? chapa : join(" ",
                chapa,
                orden.getVehiculo().getMarca(),
                orden.getVehiculo().getModelo());
        BigDecimal presupuesto = orden.getDiagnostico() != null ? orden.getDiagnostico().getTotalPresupuesto() : null;
        String falla = orden.getRecepcion() != null ? nvl(orden.getRecepcion().getDescripcionFalla()) : "";
        String extra = historial
                ? nvl(formatFechaHora(orden.getFechaFinalizacion()))
                : chapa;
        return ReporteFila.builder()
                .numero(numero)
                .codigo(nvl(orden.getNumeroOrden()))
                .nombre(firstNonBlank(nombreCompleto(clientePersona), vehiculo))
                .descripcion(falla)
                .categoria(nvl(orden.getEtapa()))
                .subcategoria(orden.getSector() != null ? nvl(orden.getSector().getNombre()) : "")
                .precio(formatGs(presupuesto))
                .extra(extra)
                .estado(nvl(orden.getEtapa()))
                .build();
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

    private static String formatFechaHora(LocalDateTime value) {
        return DateUtil.format(value);
    }

    private static String[] resolverCategoriaProducto(CategoriaProducto categoria) {
        if (categoria == null) {
            return new String[] {"", ""};
        }
        if (categoria.getCategoriaPadre() != null) {
            return new String[] {
                    nvl(categoria.getCategoriaPadre().getNombre()),
                    nvl(categoria.getNombre())
            };
        }
        return new String[] { nvl(categoria.getNombre()), "" };
    }

    private static String[] resolverCategoriaServicio(CategoriaServicio categoria) {
        if (categoria == null) {
            return new String[] {"", ""};
        }
        if (categoria.getCategoriaPadre() != null) {
            return new String[] {
                    nvl(categoria.getCategoriaPadre().getNombre()),
                    nvl(categoria.getNombre())
            };
        }
        return new String[] { nvl(categoria.getNombre()), "" };
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

    private static String estado(boolean activo) {
        return activo ? AppConstants.Estados.ACTIVO : AppConstants.Estados.INACTIVO;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
