package com.dev.ultron.dto.compras.mapper;

import com.dev.ultron.domain.compras.Proveedor;
import com.dev.ultron.domain.compras.Vendedor;
import com.dev.ultron.dto.compras.input.VendedorInput;
import com.dev.ultron.dto.compras.output.VendedorOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import com.dev.ultron.generic.mapper.UpdatableMapper;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface VendedorMapper extends BaseMapper<Vendedor, VendedorInput, VendedorOutput>, 
                                         UpdatableMapper<Vendedor, VendedorInput> {

    @Mapping(target = "id_vendedor", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "apellido", source = "apellido", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "documento", source = "documento", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "telefono", source = "telefono")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "celular", source = "celular")
    @Mapping(target = "cargo", source = "cargo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "observaciones", source = "observaciones")
    @Mapping(target = "activo", source = "activo", qualifiedByName = MappingHelper.DEFAULT_BOOLEAN_TRUE)
    @Mapping(target = "fecha_registro", ignore = true)
    @Mapping(target = "fecha_ultima_modificacion", ignore = true)
    Vendedor toEntity(VendedorInput input);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_vendedor", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "apellido", source = "apellido", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "documento", source = "documento", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "telefono", source = "telefono")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "celular", source = "celular")
    @Mapping(target = "cargo", source = "cargo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "observaciones", source = "observaciones")
    @Mapping(target = "fecha_registro", ignore = true)
    @Mapping(target = "fecha_ultima_modificacion", ignore = true)
    void updateEntity(@MappingTarget Vendedor vendedor, VendedorInput input);

    @Override
    @Mapping(target = "id_proveedor", source = "proveedor.id_proveedor")
    @Mapping(target = "proveedor_nombre", expression = "java(getProveedorNombre(vendedor.getProveedor()))")
    @Mapping(target = "fecha_registro", source = "fecha_registro", qualifiedByName = MappingHelper.FORMAT_DATETIME)
    @Mapping(target = "fecha_ultima_modificacion", source = "fecha_ultima_modificacion", qualifiedByName = MappingHelper.FORMAT_DATETIME)
    @Mapping(target = "nombre_completo", expression = "java(getNombreCompleto(vendedor))")
    VendedorOutput toOutput(Vendedor vendedor);

    default String getNombreCompleto(Vendedor vendedor) {
        if (vendedor == null) return null;
        return (vendedor.getNombre() != null ? vendedor.getNombre() : "") + " " + 
               (vendedor.getApellido() != null ? vendedor.getApellido() : "");
    }

    default String getProveedorNombre(Proveedor proveedor) {
        if (proveedor == null) return null;
        
        if (proveedor.getTipo_persona() == Proveedor.TipoPersona.FISICA) {
            return (proveedor.getNombre() != null ? proveedor.getNombre() : "") + " " + 
                   (proveedor.getApellido() != null ? proveedor.getApellido() : "");
        } else {
            return proveedor.getRazon_social();
        }
    }
}
