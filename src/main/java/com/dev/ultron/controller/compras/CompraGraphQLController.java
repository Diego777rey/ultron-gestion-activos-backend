package com.dev.ultron.controller.compras;

import com.dev.ultron.dto.compras.input.CompraInput;
import com.dev.ultron.dto.compras.output.CompraOutput;
import com.dev.ultron.dto.compras.output.ItemCompraOutput;
import com.dev.ultron.dto.compras.output.ProductoProveedorOutput;
import com.dev.ultron.service.compras.CompraService;
import com.dev.ultron.service.compras.ProductoProveedorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CompraGraphQLController {

    private final CompraService compraService;
    private final ProductoProveedorService productoProveedorService;

    // ========== QUERIES ==========

    @QueryMapping
    public List<CompraOutput> compras() {
        log.info("GraphQL Query: compras");
        return compraService.listarCompras();
    }

    @QueryMapping
    public CompraOutput compra(@Argument Long id) {
        log.info("GraphQL Query: compra(id={})", id);
        return compraService.obtenerCompra(id);
    }

    @QueryMapping
    public List<CompraOutput> comprasPorProveedor(@Argument Long idProveedor) {
        log.info("GraphQL Query: comprasPorProveedor(idProveedor={})", idProveedor);
        return compraService.listarComprasPorProveedor(idProveedor);
    }

    @QueryMapping
    public List<CompraOutput> comprasPorEstado(@Argument String estado) {
        log.info("GraphQL Query: comprasPorEstado(estado={})", estado);
        return compraService.listarComprasPorEstado(estado);
    }

    @QueryMapping
    public List<CompraOutput> comprasPorRangoFecha(@Argument String fechaInicio, @Argument String fechaFin) {
        log.info("GraphQL Query: comprasPorRangoFecha(fechaInicio={}, fechaFin={})", fechaInicio, fechaFin);
        return compraService.listarComprasPorRangoFecha(fechaInicio, fechaFin);
    }

    @QueryMapping
    public List<ProductoProveedorOutput> productosDeProveedor(@Argument Long idProveedor) {
        log.info("GraphQL Query: productosDeProveedor(idProveedor={})", idProveedor);
        return productoProveedorService.listarProductosDeProveedor(idProveedor);
    }

    @QueryMapping
    public List<ProductoProveedorOutput> proveedoresDeProducto(@Argument Long idProducto) {
        log.info("GraphQL Query: proveedoresDeProducto(idProducto={})", idProducto);
        return productoProveedorService.listarProveedoresDeProducto(idProducto);
    }

    @QueryMapping
    public List<ItemCompraOutput> historialCompraProducto(@Argument Long idProducto, @Argument Long idProveedor) {
        log.info("GraphQL Query: historialCompraProducto(idProducto={}, idProveedor={})", idProducto, idProveedor);
        return productoProveedorService.obtenerHistorialCompraProducto(idProducto, idProveedor);
    }

    // ========== MUTATIONS ==========

    @MutationMapping
    public CompraOutput registrarCompra(@Argument CompraInput input) {
        log.info("GraphQL Mutation: registrarCompra");
        return compraService.registrarCompra(input);
    }

    @MutationMapping
    public CompraOutput actualizarCompra(@Argument Long id, @Argument CompraInput input) {
        log.info("GraphQL Mutation: actualizarCompra(id={})", id);
        return compraService.actualizarCompra(id, input);
    }

    @MutationMapping
    public CompraOutput cambiarEstadoCompra(@Argument Long id, @Argument String estado) {
        log.info("GraphQL Mutation: cambiarEstadoCompra(id={}, estado={})", id, estado);
        return compraService.cambiarEstadoCompra(id, estado);
    }

    @MutationMapping
    public ProductoProveedorOutput asociarProductoProveedor(@Argument Long idProducto, @Argument Long idProveedor) {
        log.info("GraphQL Mutation: asociarProductoProveedor(idProducto={}, idProveedor={})", idProducto, idProveedor);
        return productoProveedorService.asociar(idProducto, idProveedor);
    }

    @MutationMapping
    public Boolean desasociarProductoProveedor(@Argument Long idProducto, @Argument Long idProveedor) {
        log.info("GraphQL Mutation: desasociarProductoProveedor(idProducto={}, idProveedor={})", idProducto, idProveedor);
        return productoProveedorService.desasociar(idProducto, idProveedor);
    }

    @MutationMapping
    public Boolean eliminarCompra(@Argument Long id) {
        log.info("GraphQL Mutation: eliminarCompra(id={})", id);
        return compraService.eliminarCompra(id);
    }
}
