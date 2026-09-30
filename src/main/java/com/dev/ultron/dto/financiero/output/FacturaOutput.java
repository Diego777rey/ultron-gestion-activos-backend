package com.dev.ultron.dto.financiero.output;

import com.dev.ultron.dto.personas.output.ClienteOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Output DTO de Factura para respuestas GraphQL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaOutput implements Serializable {
    private Long id_factura;
    private String numero_factura;
    private String timbrado;
    private String fecha_emision;
    
    private Long id_timbrado;
    private Long id_cliente;
    private Long id_venta;
    private Long id_sesion_caja;
    private Long id_empresa;
    
    private ClienteOutput cliente;
    
    private String cliente_nombre;
    private String cliente_documento;
    private String cliente_ruc;
    private String cliente_direccion;
    
    private BigDecimal subtotal;
    private BigDecimal total_iva_5;
    private BigDecimal total_iva_10;
    private BigDecimal total_exenta;
    private BigDecimal total_iva;
    private BigDecimal total;
    
    private String forma_pago;
    private String moneda;
    
    private String estado;
    private String fecha_anulacion;
    private String motivo_anulacion;
    private Long id_usuario_anulacion;
    
    private String observaciones;
    
    private Long id_usuario_emisor;
    private String fecha_creacion;
    
    private List<DetalleFacturaOutput> detalles;
}
