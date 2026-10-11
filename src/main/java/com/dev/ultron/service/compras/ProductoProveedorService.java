package com.dev.ultron.service.compras;

import com.dev.ultron.domain.compras.ItemCompra;
import com.dev.ultron.domain.compras.ProductoProveedor;
import com.dev.ultron.dto.compras.mapper.ItemCompraMapper;
import com.dev.ultron.dto.compras.mapper.ProductoProveedorMapper;
import com.dev.ultron.dto.compras.output.ItemCompraOutput;
import com.dev.ultron.dto.compras.output.ProductoProveedorOutput;
import com.dev.ultron.repository.compras.ItemCompraRepository;
import com.dev.ultron.repository.compras.ProductoProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoProveedorService {

    private final ProductoProveedorRepository productoProveedorRepository;
    private final ItemCompraRepository itemCompraRepository;
    private final ProductoProveedorMapper productoProveedorMapper;
    private final ItemCompraMapper itemCompraMapper;

    @Transactional(readOnly = true)
    public List<ProductoProveedorOutput> listarProductosDeProveedor(Long idProveedor) {
        List<ProductoProveedor> productos = productoProveedorRepository.findByProveedorActivos(idProveedor);
        return productoProveedorMapper.toOutputList(productos);
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
}
