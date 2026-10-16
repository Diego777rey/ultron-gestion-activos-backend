package com.dev.ultron.dto.compras.mapper;

import com.dev.ultron.domain.compras.Compra;
import com.dev.ultron.dto.compras.input.CompraInput;
import com.dev.ultron.dto.compras.output.CompraOutput;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemCompraMapper.class})
public interface CompraMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(target = "id_compra", source = "id_compra")
    @Mapping(target = "id_proveedor", source = "proveedor.id_proveedor")
    @Mapping(target = "proveedor_nombre", expression = "java(getNombreCompleto(compra))")
    @Mapping(target = "id_vendedor", source = "vendedor.id_vendedor")
    @Mapping(target = "vendedor_nombre", expression = "java(getVendedorNombre(compra))")
    @Mapping(target = "id_sector", source = "sector.id_sector")
    @Mapping(target = "sector_nombre", source = "sector.nombre")
    @Mapping(target = "fecha_factura", expression = "java(formatDate(compra.getFecha_factura()))")
    @Mapping(target = "fecha_compra", expression = "java(formatDateTime(compra.getFecha_compra()))")
    @Mapping(target = "estado", expression = "java(compra.getEstado().name())")
    @Mapping(target = "fecha_registro", expression = "java(formatDateTime(compra.getFecha_registro()))")
    @Mapping(target = "fecha_ultima_modificacion", expression = "java(formatDateTime(compra.getFecha_ultima_modificacion()))")
    CompraOutput toOutput(Compra compra);

    List<CompraOutput> toOutputList(List<Compra> compras);

    @Mapping(target = "id_compra", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "sector", ignore = true)
    @Mapping(target = "fecha_compra", ignore = true)
    @Mapping(target = "fecha_registro", ignore = true)
    @Mapping(target = "fecha_ultima_modificacion", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "estado", expression = "java(parseEstado(input.estado()))")
    @Mapping(target = "moneda", source = "moneda", defaultValue = "PYG")
    @Mapping(target = "numero_factura", source = "numeroFactura")
    @Mapping(target = "fecha_factura", source = "fechaFactura")
    @Mapping(target = "forma_pago", source = "formaPago")
    Compra toEntity(CompraInput input);

    @Mapping(target = "id_compra", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "vendedor", ignore = true)
    @Mapping(target = "sector", ignore = true)
    @Mapping(target = "fecha_compra", ignore = true)
    @Mapping(target = "fecha_registro", ignore = true)
    @Mapping(target = "fecha_ultima_modificacion", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "estado", expression = "java(parseEstado(input.estado()))")
    @Mapping(target = "numero_factura", source = "numeroFactura")
    @Mapping(target = "fecha_factura", source = "fechaFactura")
    @Mapping(target = "forma_pago", source = "formaPago")
    void updateEntity(@MappingTarget Compra compra, CompraInput input);

    default String getNombreCompleto(Compra compra) {
        if (compra.getProveedor() == null) {
            return null;
        }
        var proveedor = compra.getProveedor();
        if (proveedor.getTipo_persona() == com.dev.ultron.domain.compras.Proveedor.TipoPersona.FISICA) {
            return (proveedor.getNombre() + " " + proveedor.getApellido()).trim();
        } else {
            return proveedor.getRazon_social();
        }
    }

    default String getVendedorNombre(Compra compra) {
        if (compra.getVendedor() == null) {
            return null;
        }
        return (compra.getVendedor().getNombre() + " " + compra.getVendedor().getApellido()).trim();
    }

    default String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    default String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    default Compra.EstadoCompra parseEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return Compra.EstadoCompra.BORRADOR;
        }
        try {
            return Compra.EstadoCompra.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Compra.EstadoCompra.BORRADOR;
        }
    }
}
