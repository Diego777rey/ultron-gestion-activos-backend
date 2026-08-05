package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import com.dev.ultron.dto.taller.input.OrdenTrabajoDetalleInput;
import com.dev.ultron.dto.taller.mapper.OrdenTrabajoMapper;
import com.dev.ultron.dto.taller.output.OrdenTrabajoOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.inventario.ServicioRepository;
import com.dev.ultron.repository.taller.OrdenTrabajoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Gestiona líneas de presupuesto (productos / servicios) de la orden.
 */
@Service
public class OrdenTrabajoDetalleService {

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenTrabajoMapper ordenTrabajoMapper;
    private final OrdenDiagnosticoWriter diagnosticoWriter;
    private final ProductoRepository productoRepo;
    private final ServicioRepository servicioRepo;

    public OrdenTrabajoDetalleService(
            OrdenTrabajoRepository ordenTrabajoRepository,
            OrdenTrabajoMapper ordenTrabajoMapper,
            OrdenDiagnosticoWriter diagnosticoWriter,
            ProductoRepository productoRepo,
            ServicioRepository servicioRepo) {
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.ordenTrabajoMapper = ordenTrabajoMapper;
        this.diagnosticoWriter = diagnosticoWriter;
        this.productoRepo = productoRepo;
        this.servicioRepo = servicioRepo;
    }

    @Transactional
    public OrdenTrabajoOutput agregarDetalle(Long idOrden, OrdenTrabajoDetalleInput input) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        exigirEtapaEditable(orden.getEtapa());

        OrdenTrabajoDetalle detalle = OrdenTrabajoDetalle.builder()
                .ordenTrabajo(orden)
                .tipo(input.tipo().toUpperCase())
                .descripcion(input.descripcion() != null ? input.descripcion().toUpperCase() : null)
                .cantidad(input.cantidad() != null ? input.cantidad() : BigDecimal.ONE)
                .precioUnitario(input.precio_unitario() != null ? input.precio_unitario() : BigDecimal.ZERO)
                .etapaOrigen(orden.getEtapa())
                .build();

        if ("PRODUCTO".equalsIgnoreCase(input.tipo())) {
            aplicarProducto(detalle, input);
        } else if ("SERVICIO".equalsIgnoreCase(input.tipo())) {
            aplicarServicio(detalle, input);
        } else {
            throw new IllegalArgumentException(
                    "Tipo de detalle inválido: " + input.tipo() + ". Debe ser PRODUCTO o SERVICIO.");
        }

        detalle.setSubtotal(detalle.getCantidad().multiply(detalle.getPrecioUnitario()));
        orden.getDetalles().add(detalle);
        diagnosticoWriter.recalcularTotalPresupuesto(orden);
        return ordenTrabajoMapper.toOutput(ordenTrabajoRepository.save(orden));
    }

    @Transactional
    public OrdenTrabajoOutput eliminarDetalle(Long idOrden, Long idDetalle) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        exigirEtapaEditable(orden.getEtapa());

        boolean removed = orden.getDetalles().removeIf(d -> d.getId_detalle().equals(idDetalle));
        if (!removed) {
            throw new EntityNotFoundException("Detalle no encontrado con ID: " + idDetalle);
        }
        diagnosticoWriter.recalcularTotalPresupuesto(orden);
        return ordenTrabajoMapper.toOutput(ordenTrabajoRepository.save(orden));
    }

    private void aplicarProducto(OrdenTrabajoDetalle detalle, OrdenTrabajoDetalleInput input) {
        if (input.id_producto() == null) {
            throw new IllegalArgumentException("El ID del producto es obligatorio para tipo PRODUCTO");
        }
        Producto producto = productoRepo.findById(input.id_producto())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + input.id_producto()));
        detalle.setProducto(producto);
        if (detalle.getDescripcion() == null) {
            detalle.setDescripcion(producto.getNombre());
        }
        if (input.precio_unitario() == null) {
            detalle.setPrecioUnitario(producto.getPrecioVenta());
        }
    }

    private void aplicarServicio(OrdenTrabajoDetalle detalle, OrdenTrabajoDetalleInput input) {
        if (input.id_servicio() == null) {
            throw new IllegalArgumentException("El ID del servicio es obligatorio para tipo SERVICIO");
        }
        Servicio servicio = servicioRepo.findById(input.id_servicio())
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado: " + input.id_servicio()));
        detalle.setServicio(servicio);
        if (detalle.getDescripcion() == null) {
            detalle.setDescripcion(servicio.getNombre());
        }
        if (input.precio_unitario() == null) {
            detalle.setPrecioUnitario(servicio.getPrecio());
        }
    }

    private void exigirEtapaEditable(String etapa) {
        if (!"DIAGNOSTICO".equalsIgnoreCase(etapa) && !"EN_PROCESO".equalsIgnoreCase(etapa)) {
            throw new IllegalArgumentException(
                    "Solo se pueden modificar detalles en DIAGNOSTICO o EN_PROCESO. Etapa actual: " + etapa);
        }
    }

    private OrdenTrabajo buscarPorIdOrThrow(Long id) {
        return ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden de trabajo no encontrada: " + id));
    }
}
