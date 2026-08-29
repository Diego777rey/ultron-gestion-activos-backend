package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.inventario.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class ProductoReporteFuente implements ReporteFuente {

    private final ProductoRepository productoRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.PRODUCTO;
    }

    @Override
    public String titulo() {
        return "Catálogo de productos";
    }

    @Override
    public String subtitulo() {
        return "Listado genérico de inventario";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "STOCK";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-productos.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        List<Producto> productos;
        if (filtro != null && !filtro.isBlank()) {
            productos = productoRepository.buscarParaReporte(filtro.trim());
        } else {
            productos = productoRepository.findAllParaReporte();
        }
        return mapear(productos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        Producto producto = productoRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));
        return mapear(List.of(producto));
    }

    private List<ReporteFila> mapear(List<Producto> productos) {
        AtomicInteger numero = new AtomicInteger(1);
        return productos.stream()
                .map(producto -> ReporteFilaMapper.deProducto(producto, numero.getAndIncrement()))
                .toList();
    }
}
