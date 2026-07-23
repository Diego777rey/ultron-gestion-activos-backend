package com.dev.ultron.dto.taller.mapper;

import com.dev.ultron.domain.taller.SolicitudRepuesto;
import com.dev.ultron.domain.taller.SolicitudRepuestoDetalle;
import com.dev.ultron.dto.sectores.mapper.SectorMapper;
import com.dev.ultron.dto.taller.output.SolicitudRepuestoDetalleOutput;
import com.dev.ultron.dto.taller.output.SolicitudRepuestoOutput;
import com.dev.ultron.generic.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(config = MapStructConfig.class, uses = {SectorMapper.class})
public interface SolicitudRepuestoMapper {

    @Mapping(target = "id_solicitud_repuesto", source = "id_solicitud_repuesto")
    @Mapping(target = "id_orden_trabajo", expression = "java(entity.getOrdenTrabajo() != null ? entity.getOrdenTrabajo().getId_orden_trabajo() : null)")
    @Mapping(target = "numero_orden", expression = "java(entity.getOrdenTrabajo() != null ? entity.getOrdenTrabajo().getNumeroOrden() : null)")
    @Mapping(target = "sector_origen", source = "sectorOrigen")
    @Mapping(target = "sector_destino", source = "sectorDestino")
    @Mapping(target = "motivo_rechazo", source = "motivoRechazo")
    @Mapping(target = "fecha", source = "fecha", qualifiedByName = "formatDateTime")
    @Mapping(target = "id_transferencia", expression = "java(entity.getTransferencia() != null ? entity.getTransferencia().getId_transferencia() : null)")
    @Mapping(target = "numero_transferencia", expression = "java(entity.getTransferencia() != null ? entity.getTransferencia().getNumero() : null)")
    @Mapping(target = "detalles", source = "detalles")
    SolicitudRepuestoOutput toOutput(SolicitudRepuesto entity);

    List<SolicitudRepuestoOutput> toOutputList(List<SolicitudRepuesto> entities);

    @Mapping(target = "id_detalle", source = "id_detalle")
    @Mapping(target = "id_producto", expression = "java(detalle.getProducto() != null ? detalle.getProducto().getId_producto() : null)")
    @Mapping(target = "nombre_producto", expression = "java(detalle.getProducto() != null ? detalle.getProducto().getNombre() : null)")
    @Mapping(target = "codigo_producto", expression = "java(detalle.getProducto() != null ? detalle.getProducto().getCodigo() : null)")
    SolicitudRepuestoDetalleOutput toDetalleOutput(SolicitudRepuestoDetalle detalle);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
