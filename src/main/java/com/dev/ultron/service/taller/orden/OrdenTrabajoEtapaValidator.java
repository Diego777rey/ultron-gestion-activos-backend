package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.taller.OrdenDiagnostico;
import com.dev.ultron.domain.taller.OrdenTrabajo;

import org.springframework.stereotype.Component;

/**
 * Valida transiciones y requisitos de etapa de una orden de trabajo.
 */
@Component
public class OrdenTrabajoEtapaValidator {

    private final OrdenTrabajoActoresWriter actoresWriter;
    private final OrdenTrabajoCajaResolver cajaResolver;

    public OrdenTrabajoEtapaValidator(
            OrdenTrabajoActoresWriter actoresWriter,
            OrdenTrabajoCajaResolver cajaResolver) {
        this.actoresWriter = actoresWriter;
        this.cajaResolver = cajaResolver;
    }

    public void validarTransicion(String etapaActual, String nuevaEtapa) {
        boolean valida = switch (etapaActual) {
            case "RECEPCION" -> "DIAGNOSTICO".equalsIgnoreCase(nuevaEtapa);
            case "DIAGNOSTICO" -> "EN_PROCESO".equalsIgnoreCase(nuevaEtapa);
            case "EN_PROCESO" -> "FINALIZADA".equalsIgnoreCase(nuevaEtapa);
            case "FINALIZADA" -> "FACTURADO".equalsIgnoreCase(nuevaEtapa);
            default -> false;
        };
        if (!valida) {
            throw new IllegalArgumentException(
                    "Transición de etapa inválida: " + etapaActual + " → " + nuevaEtapa);
        }
    }

    public void validarRequisitos(OrdenTrabajo orden, String nuevaEtapa) {
        switch (nuevaEtapa) {
            case "DIAGNOSTICO" -> validarParaDiagnostico(orden);
            case "EN_PROCESO" -> validarParaEnProceso(orden);
            case "FINALIZADA" -> validarParaFinalizada(orden);
            case "FACTURADO" -> validarParaFacturado(orden);
            default -> {
            }
        }
    }

    private void validarParaDiagnostico(OrdenTrabajo orden) {
        if (orden.getCliente() == null) {
            throw new IllegalArgumentException("Debe asignar un cliente antes de pasar a Diagnóstico");
        }
        if (orden.getVehiculo() == null) {
            throw new IllegalArgumentException("Debe asignar un vehículo antes de pasar a Diagnóstico");
        }
        if (orden.getMecanico() == null) {
            throw new IllegalArgumentException("Debe asignar un mecánico antes de pasar a Diagnóstico");
        }
        String descripcion = orden.getRecepcion() != null
                ? orden.getRecepcion().getDescripcionFalla()
                : null;
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Debe registrar la descripción de la falla");
        }
        actoresWriter.validarVehiculoPerteneceACliente(orden.getCliente(), orden.getVehiculo());
    }

    private void validarParaEnProceso(OrdenTrabajo orden) {
        if (orden.getHallazgos() == null || orden.getHallazgos().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe registrar al menos un fallo o defecto encontrado en el diagnóstico");
        }
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El presupuesto debe tener al menos un ítem");
        }
        var diagnostico = orden.getDiagnostico();
        boolean aprobado = diagnostico != null && diagnostico.isPresupuestoAprobado();
        if (!aprobado) {
            throw new IllegalArgumentException("El presupuesto debe estar aprobado por el cliente");
        }
        validarPlazos(diagnostico);
    }

    private void validarPlazos(OrdenDiagnostico diagnostico) {
        boolean tieneInicio = diagnostico != null && diagnostico.getFechaInicioEstimada() != null;
        boolean tieneFin = diagnostico != null && diagnostico.getFechaFinEstimada() != null;
        boolean tieneDuracion = diagnostico != null
                && diagnostico.getDuracionEstimadaDias() != null
                && diagnostico.getDuracionEstimadaDias() > 0;
        if (!(tieneInicio && tieneFin) && !tieneDuracion) {
            throw new IllegalArgumentException(
                    "Debe indicar fecha de inicio y fin estimadas, o el tiempo que llevará el trabajo");
        }
    }

    private void validarParaFinalizada(OrdenTrabajo orden) {
        if (orden.getCaja() == null) {
            throw new IllegalArgumentException(
                    "Debe asignar una caja abierta antes de finalizar (use enviarOrdenACaja)");
        }
        if (!cajaResolver.tieneSesionAbierta(orden.getCaja().getId_caja())) {
            throw new IllegalArgumentException("La caja asignada no tiene sesión abierta");
        }
    }

    private void validarParaFacturado(OrdenTrabajo orden) {
        if (orden.getCaja() == null) {
            throw new IllegalArgumentException("La orden debe tener una caja asignada para facturar");
        }
    }
}
