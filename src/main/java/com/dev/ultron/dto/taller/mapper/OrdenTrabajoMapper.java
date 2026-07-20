package com.dev.ultron.dto.taller.mapper;

import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import com.dev.ultron.dto.taller.output.OrdenTrabajoDetalleOutput;
import com.dev.ultron.dto.taller.output.OrdenTrabajoOutput;
import com.dev.ultron.dto.personas.mapper.ClienteMapper;
import com.dev.ultron.dto.personas.mapper.FuncionarioMapper;
import com.dev.ultron.dto.personas.mapper.UsuarioMapper;
import com.dev.ultron.dto.patrimonio.mapper.VehiculoMapper;
import com.dev.ultron.dto.sectores.mapper.SectorMapper;
import com.dev.ultron.dto.financiero.mapper.CajaMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(config = MapStructConfig.class, uses = {
        ClienteMapper.class,
        VehiculoMapper.class,
        FuncionarioMapper.class,
        SectorMapper.class,
        UsuarioMapper.class,
        CajaMapper.class
})
public interface OrdenTrabajoMapper {

    @Mapping(target = "id_orden_trabajo", source = "id_orden_trabajo")
    @Mapping(target = "numero_orden", source = "numeroOrden")
    @Mapping(target = "descripcion_falla", source = "descripcionFalla")
    @Mapping(target = "fecha_inicio_estimada", source = "fechaInicioEstimada", qualifiedByName = "formatDateTime")
    @Mapping(target = "fecha_fin_estimada", source = "fechaFinEstimada", qualifiedByName = "formatDateTime")
    @Mapping(target = "fecha_creacion", source = "fechaCreacion", qualifiedByName = "formatDateTime")
    @Mapping(target = "fecha_finalizacion", source = "fechaFinalizacion", qualifiedByName = "formatDateTime")
    @Mapping(target = "presupuesto_aprobado", source = "presupuestoAprobado")
    @Mapping(target = "total_presupuesto", source = "totalPresupuesto")
    @Mapping(target = "detalles", source = "detalles")
    OrdenTrabajoOutput toOutput(OrdenTrabajo ordenTrabajo);

    List<OrdenTrabajoOutput> toOutputList(List<OrdenTrabajo> ordenes);

    @Mapping(target = "id_detalle", source = "id_detalle")
    @Mapping(target = "id_producto", expression = "java(detalle.getProducto() != null ? detalle.getProducto().getId_producto() : null)")
    @Mapping(target = "nombre_producto", expression = "java(detalle.getProducto() != null ? detalle.getProducto().getNombre() : null)")
    @Mapping(target = "id_servicio", expression = "java(detalle.getServicio() != null ? detalle.getServicio().getId_servicio() : null)")
    @Mapping(target = "nombre_servicio", expression = "java(detalle.getServicio() != null ? detalle.getServicio().getNombre() : null)")
    @Mapping(target = "precio_unitario", source = "precioUnitario")
    @Mapping(target = "etapa_origen", source = "etapaOrigen")
    OrdenTrabajoDetalleOutput toDetalleOutput(OrdenTrabajoDetalle detalle);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
