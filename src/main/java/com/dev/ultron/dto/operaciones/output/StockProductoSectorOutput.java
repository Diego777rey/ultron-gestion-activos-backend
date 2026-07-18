package com.dev.ultron.dto.operaciones.output;

import com.dev.ultron.dto.inventario.output.ProductoOutput;
import com.dev.ultron.dto.sectores.output.SectorOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockProductoSectorOutput implements Serializable {
    private Long id_stock;
    private ProductoOutput producto;
    private SectorOutput sector;
    private BigDecimal cantidad;
}
