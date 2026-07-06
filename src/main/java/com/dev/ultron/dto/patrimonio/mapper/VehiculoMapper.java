package com.dev.ultron.dto.patrimonio.mapper;

import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.dto.patrimonio.input.VehiculoInput;
import com.dev.ultron.dto.patrimonio.output.VehiculoOutput;
import com.dev.ultron.dto.personas.mapper.ClienteMapper;
import com.dev.ultron.utilitarios.AppConstants;
import com.dev.ultron.utilitarios.StringUtil;

/**
 * Mapper para convertir entre Vehiculo entity, input y output.
 * Reutiliza ClienteMapper para los datos del cliente propietario.
 */
public class VehiculoMapper {

    private static final String TIPO_BIEN_VEHICULO = "VEHICULO";

    private VehiculoMapper() {
    }

    public static Vehiculo toEntity(VehiculoInput input, Cliente cliente) {
        if (input == null) {
            return null;
        }
        return Vehiculo.builder()
                .tipo(TIPO_BIEN_VEHICULO)
                .descripcion(StringUtil.toUpperCase(input.descripcion()))
                .valor(input.valor())
                .fecha_adquisicion(input.fecha_adquisicion())
                .id_empresa(input.id_empresa())
                .estado(resolveEstado(input.estado()))
                .cliente(cliente)
                .marca(StringUtil.toUpperCase(input.marca()))
                .modelo(StringUtil.toUpperCase(input.modelo()))
                .anio(input.anio())
                .chapa(StringUtil.toUpperCase(input.chapa()))
                .tipoVehiculo(StringUtil.toUpperCase(input.tipo_vehiculo()))
                .build();
    }

    public static void updateEntity(Vehiculo vehiculo, VehiculoInput input, Cliente cliente) {
        if (input == null || vehiculo == null) {
            return;
        }
        vehiculo.setDescripcion(StringUtil.toUpperCase(input.descripcion()));
        vehiculo.setValor(input.valor());
        vehiculo.setFecha_adquisicion(input.fecha_adquisicion());
        vehiculo.setId_empresa(input.id_empresa());
        if (input.estado() != null) {
            vehiculo.setEstado(StringUtil.toUpperCase(input.estado()));
        }
        vehiculo.setCliente(cliente);
        vehiculo.setMarca(StringUtil.toUpperCase(input.marca()));
        vehiculo.setModelo(StringUtil.toUpperCase(input.modelo()));
        vehiculo.setAnio(input.anio());
        vehiculo.setChapa(StringUtil.toUpperCase(input.chapa()));
        vehiculo.setTipoVehiculo(StringUtil.toUpperCase(input.tipo_vehiculo()));
    }

    public static VehiculoOutput toOutput(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return null;
        }
        return VehiculoOutput.builder()
                .id_bien(vehiculo.getId_bien())
                .tipo(vehiculo.getTipo())
                .descripcion(vehiculo.getDescripcion())
                .valor(vehiculo.getValor())
                .fecha_adquisicion(vehiculo.getFecha_adquisicion())
                .id_empresa(vehiculo.getId_empresa())
                .estado(vehiculo.getEstado())
                .cliente(ClienteMapper.toOutput(vehiculo.getCliente()))
                .marca(vehiculo.getMarca())
                .modelo(vehiculo.getModelo())
                .anio(vehiculo.getAnio())
                .chapa(vehiculo.getChapa())
                .tipo_vehiculo(vehiculo.getTipoVehiculo())
                .build();
    }

    private static String resolveEstado(String estado) {
        if (StringUtil.isNullOrEmpty(estado)) {
            return AppConstants.Estados.ACTIVO;
        }
        return StringUtil.toUpperCase(estado);
    }
}
