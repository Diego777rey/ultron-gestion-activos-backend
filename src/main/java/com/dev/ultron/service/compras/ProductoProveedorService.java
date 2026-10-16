package com.dev.ultron.service.compras;

import com.dev.ultron.domain.compras.ItemCompra;
import com.dev.ultron.domain.compras.ProductoProveedor;
import com.dev.ultron.domain.compras.Proveedor;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.dto.compras.mapper.ItemCompraMapper;
import com.dev.ultron.dto.compras.mapper.ProductoProveedorMapper;
import com.dev.ultron.dto.compras.output.ItemCompraOutput;
import com.dev.ultron.dto.compras.output.ProductoProveedorOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.compras.ItemCompraRepository;
import com.dev.ultron.repository.compras.ProductoProveedorRepository;
import com.dev.ultron.repository.compras.ProveedorRepository;
import com.dev.ultron.repository.inventario.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoProveedorService {

    private final ProductoProveedorRepository productoProveedorRepository;
    private final ItemCompraRepository itemCompraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoProveedorMapper productoProveedorMapper;
    private final ItemCompraMapper itemCompraMapper;

    @Transactional(readOnly = true)
    public List<ProductoProveedorOutput> listarProductosDeProveedor(Long idProveedor) {
        List<ProductoProveedor> productos = productoProveedorRepository.findByProveedorActivos(idProveedor);
        return productoProveedorMapper.toOutputList(productos);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoProveedorOutput> listarProductosDeProveedor(Long idProveedor, int page, int size, String filter) {
        int pagina = Math.max(page, 0);
        int tamano = Math.min(Math.max(size, 1), 100);
        String filtro = filter == null ? "" : filter.trim();
        return new PageResponse<>(productoProveedorRepository
            .buscarPorProveedor(idProveedor, filtro, PageRequest.of(pagina, tamano))
            .map(productoProveedorMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public List<ProductoProveedorOutput> listarProveedoresDeProducto(Long idProducto) {
        List<ProductoProveedor> proveedores = productoProveedorRepository.findByProductoActivos(idProducto);
        return productoProveedorMapper.toOutputList(proveedores);
    }

    @Transactional(readOnly = true)
    public List<ItemCompraOutput> obtenerHistorialCompraProducto(Long idProducto, Long idProveedor) {
        List<ItemCompra> historial = itemCompraRepository.findHistorialCompraProductoProveedor(idProducto, idProveedor);
        return itemCompraMapper.toOutputList(historial);
    }

    /** Asocia el producto al proveedor; si ya estaba asociado y dado de baja, lo reactiva. */
    @Transactional
    public ProductoProveedorOutput asociar(Long idProducto, Long idProveedor) {
        ProductoProveedor asociacion = productoProveedorRepository
            .findByProductoAndProveedor(idProducto, idProveedor)
            .orElseGet(() -> nuevaAsociacion(idProducto, idProveedor));
        asociacion.setActivo(true);
        return productoProveedorMapper.toOutput(productoProveedorRepository.save(asociacion));
    }

    /** Da de baja la asociación sin borrar el historial de precios. */
    @Transactional
    public boolean desasociar(Long idProducto, Long idProveedor) {
        return productoProveedorRepository.findByProductoAndProveedor(idProducto, idProveedor)
            .map(asociacion -> {
                asociacion.setActivo(false);
                productoProveedorRepository.save(asociacion);
                return true;
            })
            .orElse(false);
    }

    private ProductoProveedor nuevaAsociacion(Long idProducto, Long idProveedor) {
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Proveedor proveedor = proveedorRepository.findById(idProveedor)
            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        return ProductoProveedor.builder()
            .producto(producto)
            .proveedor(proveedor)
            .build();
    }
}
