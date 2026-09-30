package com.dev.ultron.dto.financiero.input;

import java.io.Serializable;
import java.util.List;

/**
 * Input para emitir una nueva factura.
 */
public record FacturaInput(
        Long idTimbrado,
        Long idCliente,
        Long idVenta,
        Long idSesionCaja,
        Long idEmpresa,
        String clienteNombre,
        String clienteDocumento,
        String clienteRuc,
        String clienteDireccion,
        String formaPago,
        String moneda,
        String observaciones,
        List<DetalleFacturaInput> detalles
) implements Serializable {
}
