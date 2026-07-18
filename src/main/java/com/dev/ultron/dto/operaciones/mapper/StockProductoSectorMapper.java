package com.dev.ultron.dto.operaciones.mapper;

import com.dev.ultron.domain.operaciones.StockProductoSector;
import com.dev.ultron.dto.inventario.mapper.ProductoMapper;
import com.dev.ultron.dto.operaciones.output.StockProductoSectorOutput;
import com.dev.ultron.dto.sectores.mapper.SectorMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class, uses = {ProductoMapper.class, SectorMapper.class})
public interface StockProductoSectorMapper {

    StockProductoSectorOutput toOutput(StockProductoSector entity);
}
