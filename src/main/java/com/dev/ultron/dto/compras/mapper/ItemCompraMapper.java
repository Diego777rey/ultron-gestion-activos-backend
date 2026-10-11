package com.dev.ultron.dto.compras.mapper;

import com.dev.ultron.domain.compras.ItemCompra;
import com.dev.ultron.dto.compras.input.ItemCompraInput;
import com.dev.ultron.dto.compras.output.ItemCompraOutput;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemCompraMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(target = "id_compra", source = "compra.id_compra")
    @Mapping(target = "id_producto", source = "producto.id_producto")
    @Mapping(target = "producto_nombre", source = "producto.nombre")
    @Mapping(target = "producto_codigo", source = "producto.codigo")
    @Mapping(target = "fecha_vencimiento", expression = "java(formatDate(itemCompra.getFecha_vencimiento()))")
    @Mapping(target = "fecha_registro", expression = "java(formatDateTime(itemCompra.getFecha_registro()))")
    ItemCompraOutput toOutput(ItemCompra itemCompra);

    List<ItemCompraOutput> toOutputList(List<ItemCompra> items);

    @Mapping(target = "id_item_compra", ignore = true)
    @Mapping(target = "compra", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "subtotal", expression = "java(calcularSubtotal(input))")
    @Mapping(target = "iva", expression = "java(calcularIva(input))")
    @Mapping(target = "fecha_registro", ignore = true)
    @Mapping(target = "tipo_iva", source = "tipoIva")
    @Mapping(target = "precio_costo", source = "precioCosto")
    @Mapping(target = "fecha_vencimiento", source = "fechaVencimiento")
    ItemCompra toEntity(ItemCompraInput input);

    default String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    default String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    default BigDecimal calcularSubtotal(ItemCompraInput input) {
        if (input.cantidad() == null || input.precioCosto() == null) {
            return BigDecimal.ZERO;
        }
        return input.cantidad().multiply(input.precioCosto());
    }

    default BigDecimal calcularIva(ItemCompraInput input) {
        if (input.tipoIva() == null || input.tipoIva().equals("EXENTA")) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = calcularSubtotal(input);
        BigDecimal tasaIva = switch (input.tipoIva()) {
            case "10" -> new BigDecimal("0.10");
            case "5" -> new BigDecimal("0.05");
            default -> BigDecimal.ZERO;
        };
        return subtotal.multiply(tasaIva);
    }
}
