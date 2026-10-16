package com.dev.ultron.dto.compras.mapper;

import com.dev.ultron.domain.compras.Proveedor;
import com.dev.ultron.dto.compras.input.ProveedorInput;
import com.dev.ultron.dto.compras.output.ProveedorOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import com.dev.ultron.generic.mapper.UpdatableMapper;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface ProveedorMapper extends BaseMapper<Proveedor, ProveedorInput, ProveedorOutput>, 
                                          UpdatableMapper<Proveedor, ProveedorInput> {

    @Mapping(target = "id_proveedor", ignore = true)
    @Mapping(target = "tipo_persona", source = "tipoPersona")
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "apellido", source = "apellido", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "razon_social", source = "razonSocial", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "nombre_comercial", source = "nombreComercial", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "nombre_fantasia", source = "nombreFantasia", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "ruc", source = "ruc", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "documento", source = "documento", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "direccion", source = "direccion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "telefono", source = "telefono")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "ciudad", source = "ciudad", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "pais", source = "pais", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "codigo_postal", source = "codigoPostal")
    @Mapping(target = "sitio_web", source = "sitioWeb")
    @Mapping(target = "observaciones", source = "observaciones")
    @Mapping(target = "activo", source = "activo", qualifiedByName = MappingHelper.DEFAULT_BOOLEAN_TRUE)
    @Mapping(target = "fecha_registro", ignore = true)
    @Mapping(target = "fecha_ultima_modificacion", ignore = true)
    Proveedor toEntity(ProveedorInput input);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_proveedor", ignore = true)
    @Mapping(target = "tipo_persona", source = "tipoPersona")
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "apellido", source = "apellido", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "razon_social", source = "razonSocial", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "nombre_comercial", source = "nombreComercial", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "nombre_fantasia", source = "nombreFantasia", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "ruc", source = "ruc", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "documento", source = "documento", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "direccion", source = "direccion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "telefono", source = "telefono")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "ciudad", source = "ciudad", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "pais", source = "pais", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "codigo_postal", source = "codigoPostal")
    @Mapping(target = "sitio_web", source = "sitioWeb")
    @Mapping(target = "observaciones", source = "observaciones")
    @Mapping(target = "fecha_registro", ignore = true)
    @Mapping(target = "fecha_ultima_modificacion", ignore = true)
    void updateEntity(@MappingTarget Proveedor proveedor, ProveedorInput input);

    @Override
    @Mapping(target = "tipo_persona", expression = "java(proveedor.getTipo_persona() != null ? proveedor.getTipo_persona().name() : null)")
    @Mapping(target = "fecha_registro", source = "fecha_registro", qualifiedByName = MappingHelper.FORMAT_DATETIME)
    @Mapping(target = "fecha_ultima_modificacion", source = "fecha_ultima_modificacion", qualifiedByName = MappingHelper.FORMAT_DATETIME)
    @Mapping(target = "nombre_completo", expression = "java(getNombreCompleto(proveedor))")
    ProveedorOutput toOutput(Proveedor proveedor);

    default String getNombreCompleto(Proveedor proveedor) {
        if (proveedor == null) return null;
        
        if (proveedor.getTipo_persona() == Proveedor.TipoPersona.FISICA) {
            return (proveedor.getNombre() != null ? proveedor.getNombre() : "") + " " + 
                   (proveedor.getApellido() != null ? proveedor.getApellido() : "");
        } else {
            return proveedor.getRazon_social();
        }
    }
}
