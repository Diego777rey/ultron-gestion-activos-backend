package com.dev.ultron.service.compras;

import com.dev.ultron.domain.compras.Compra;
import com.dev.ultron.domain.compras.ItemCompra;
import com.dev.ultron.domain.compras.Proveedor;
import com.dev.ultron.domain.compras.Vendedor;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.dto.compras.input.CompraInput;
import com.dev.ultron.dto.compras.input.ItemCompraInput;
import com.dev.ultron.dto.compras.mapper.CompraMapper;
import com.dev.ultron.dto.compras.mapper.ItemCompraMapper;
import com.dev.ultron.dto.compras.output.CompraOutput;
import com.dev.ultron.repository.compras.CompraRepository;
import com.dev.ultron.repository.compras.ProveedorRepository;
import com.dev.ultron.repository.compras.VendedorRepository;
import com.dev.ultron.repository.inventario.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final VendedorRepository vendedorRepository;
    private final ProductoRepository productoRepository;
    private final CompraMapper compraMapper;
    private final ItemCompraMapper itemCompraMapper;

    @Transactional(readOnly = true)
    public List<CompraOutput> listarCompras() {
        List<Compra> compras = compraRepository.findAllWithDetails();
        return compraMapper.toOutputList(compras);
    }

    @Transactional(readOnly = true)
    public CompraOutput obtenerCompra(Long id) {
        Compra compra = compraRepository.findByIdWithFull(id)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));
        return compraMapper.toOutput(compra);
    }

    @Transactional(readOnly = true)
    public List<CompraOutput> listarComprasPorProveedor(Long idProveedor) {
        List<Compra> compras = compraRepository.findByProveedor(idProveedor);
        return compraMapper.toOutputList(compras);
    }

    @Transactional(readOnly = true)
    public List<CompraOutput> listarComprasPorEstado(String estado) {
        Compra.EstadoCompra estadoCompra = Compra.EstadoCompra.valueOf(estado.toUpperCase());
        List<Compra> compras = compraRepository.findByEstado(estadoCompra);
        return compraMapper.toOutputList(compras);
    }

    @Transactional(readOnly = true)
    public List<CompraOutput> listarComprasPorRangoFecha(String fechaInicio, String fechaFin) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime inicio = LocalDateTime.parse(fechaInicio + " 00:00:00", 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime fin = LocalDateTime.parse(fechaFin + " 23:59:59", 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<Compra> compras = compraRepository.findByFechaRange(inicio, fin);
        return compraMapper.toOutputList(compras);
    }

    @Transactional
    public CompraOutput registrarCompra(CompraInput input) {
        // Validar proveedor
        Proveedor proveedor = proveedorRepository.findById(input.idProveedor())
            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        // Validar vendedor si está presente
        Vendedor vendedor = null;
        if (input.idVendedor() != null) {
            vendedor = vendedorRepository.findById(input.idVendedor())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        }

        // Crear compra
        Compra compra = compraMapper.toEntity(input);
        compra.setProveedor(proveedor);
        compra.setVendedor(vendedor);
        compra.setFecha_compra(LocalDateTime.now());

        // Procesar items
        List<ItemCompra> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        if (input.items() != null) {
            for (ItemCompraInput itemInput : input.items()) {
                Producto producto = productoRepository.findById(itemInput.idProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                ItemCompra item = itemCompraMapper.toEntity(itemInput);
                item.setCompra(compra);
                item.setProducto(producto);

                subtotal = subtotal.add(item.getSubtotal());
                if (item.getIva() != null) {
                    totalIva = totalIva.add(item.getIva());
                }

                items.add(item);
            }
        }

        compra.setItems(items);
        compra.setSubtotal(subtotal);
        compra.setTotal_iva(totalIva);
        compra.setTotal(subtotal.add(totalIva));

        // Guardar
        Compra savedCompra = compraRepository.save(compra);
        return compraMapper.toOutput(savedCompra);
    }

    @Transactional
    public CompraOutput actualizarCompra(Long id, CompraInput input) {
        Compra compra = compraRepository.findByIdWithFull(id)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));

        // Validar proveedor
        Proveedor proveedor = proveedorRepository.findById(input.idProveedor())
            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        // Validar vendedor si está presente
        Vendedor vendedor = null;
        if (input.idVendedor() != null) {
            vendedor = vendedorRepository.findById(input.idVendedor())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        }

        // Actualizar compra
        compraMapper.updateEntity(compra, input);
        compra.setProveedor(proveedor);
        compra.setVendedor(vendedor);

        // Limpiar items anteriores
        compra.getItems().clear();

        // Procesar nuevos items
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        if (input.items() != null) {
            for (ItemCompraInput itemInput : input.items()) {
                Producto producto = productoRepository.findById(itemInput.idProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                ItemCompra item = itemCompraMapper.toEntity(itemInput);
                item.setCompra(compra);
                item.setProducto(producto);

                subtotal = subtotal.add(item.getSubtotal());
                if (item.getIva() != null) {
                    totalIva = totalIva.add(item.getIva());
                }

                compra.getItems().add(item);
            }
        }

        compra.setSubtotal(subtotal);
        compra.setTotal_iva(totalIva);
        compra.setTotal(subtotal.add(totalIva));

        // Guardar
        Compra savedCompra = compraRepository.save(compra);
        return compraMapper.toOutput(savedCompra);
    }

    @Transactional
    public CompraOutput cambiarEstadoCompra(Long id, String estado) {
        Compra compra = compraRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));

        Compra.EstadoCompra nuevoEstado = Compra.EstadoCompra.valueOf(estado.toUpperCase());
        compra.setEstado(nuevoEstado);

        Compra savedCompra = compraRepository.save(compra);
        return compraMapper.toOutput(savedCompra);
    }

    @Transactional
    public boolean eliminarCompra(Long id) {
        if (!compraRepository.existsById(id)) {
            throw new RuntimeException("Compra no encontrada con ID: " + id);
        }
        compraRepository.deleteById(id);
        return true;
    }
}
