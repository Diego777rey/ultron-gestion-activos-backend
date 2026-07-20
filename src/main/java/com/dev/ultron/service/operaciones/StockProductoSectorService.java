package com.dev.ultron.service.operaciones;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.operaciones.StockProductoSector;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.inventario.mapper.ProductoMapper;
import com.dev.ultron.dto.operaciones.mapper.StockProductoSectorMapper;
import com.dev.ultron.dto.operaciones.output.StockProductoSectorOutput;
import com.dev.ultron.dto.sectores.mapper.SectorMapper;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.operaciones.StockProductoSectorRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class StockProductoSectorService {

    private final StockProductoSectorRepository repository;
    private final StockProductoSectorMapper mapper;
    private final ProductoMapper productoMapper;
    private final SectorMapper sectorMapper;
    private final ProductoRepository productoRepository;
    private final SectorRepository sectorRepository;

    public StockProductoSectorService(
            StockProductoSectorRepository repository,
            StockProductoSectorMapper mapper,
            ProductoMapper productoMapper,
            SectorMapper sectorMapper,
            ProductoRepository productoRepository,
            SectorRepository sectorRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.productoMapper = productoMapper;
        this.sectorMapper = sectorMapper;
        this.productoRepository = productoRepository;
        this.sectorRepository = sectorRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal getCantidad(Long idProducto, Long idSector) {
        return repository.findByProductoAndSector(idProducto, idSector)
                .map(StockProductoSector::getCantidad)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public StockProductoSectorOutput stockPorProductoSector(Long idProducto, Long idSector) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + idProducto));
        Sector sector = sectorRepository.findById(idSector)
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado con id: " + idSector));
        return repository.findByProductoAndSector(idProducto, idSector)
                .map(mapper::toOutput)
                .orElseGet(() -> StockProductoSectorOutput.builder()
                        .producto(productoMapper.toOutput(producto))
                        .sector(sectorMapper.toOutput(sector))
                        .cantidad(BigDecimal.ZERO)
                        .build());
    }

    @Transactional(readOnly = true)
    public java.util.List<StockProductoSectorOutput> listarPorProducto(Long idProducto) {
        return repository.findByProducto(idProducto).stream()
                .map(mapper::toOutput)
                .toList();
    }

    /**
     * Lista todos los productos con su stock en el sector (0 si no hay fila).
     * Incluye stock cero o negativo para poder transferir.
     */
    @Transactional(readOnly = true)
    public PageResponse<StockProductoSectorOutput> listarPorSector(Long idSector, int page, int size, String filter) {
        Sector sector = sectorRepository.findById(idSector)
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado con id: " + idSector));

        Page<Producto> productos = productoRepository.buscar(filter, PageRequest.of(page, size));
        return new PageResponse<>(productos.map(producto -> {
            BigDecimal cantidad = getCantidad(producto.getId_producto(), idSector);
            return repository.findByProductoAndSector(producto.getId_producto(), idSector)
                    .map(mapper::toOutput)
                    .orElseGet(() -> StockProductoSectorOutput.builder()
                            .producto(productoMapper.toOutput(producto))
                            .sector(sectorMapper.toOutput(sector))
                            .cantidad(cantidad)
                            .build());
        }));
    }

    /**
     * Ajusta el stock del producto en el sector. Delta negativo resta; positivo suma.
     * Permite resultar en stock cero o negativo.
     * Recalcula {@code producto.stock} como suma de todos los sectores.
     */
    @Transactional
    public StockProductoSector ajustar(Long idProducto, Long idSector, BigDecimal delta) {
        if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
            return obtenerOCrear(idProducto, idSector);
        }

        StockProductoSector stock = obtenerOCrear(idProducto, idSector);
        BigDecimal actual = stock.getCantidad() != null ? stock.getCantidad() : BigDecimal.ZERO;
        stock.setCantidad(actual.add(delta));
        stock = repository.save(stock);
        resyncProductoStock(idProducto);
        return stock;
    }

    @Transactional
    public void asegurarStockInicial(Producto producto, BigDecimal cantidad) {
        if (producto == null || producto.getId_producto() == null) {
            return;
        }
        BigDecimal qty = cantidad != null ? cantidad : BigDecimal.ZERO;
        if (qty.compareTo(BigDecimal.ZERO) == 0) {
            resyncProductoStock(producto.getId_producto());
            return;
        }
        Sector sector = sectorRepository.findAll(PageRequest.of(0, 1)).stream().findFirst().orElse(null);
        if (sector == null) {
            return;
        }
        boolean existe = repository.findByProductoAndSector(producto.getId_producto(), sector.getId_sector()).isPresent();
        if (!existe) {
            StockProductoSector stock = StockProductoSector.builder()
                    .producto(producto)
                    .sector(sector)
                    .cantidad(qty)
                    .build();
            repository.save(stock);
        }
        resyncProductoStock(producto.getId_producto());
    }

    @Transactional
    public void resyncProductoStock(Long idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + idProducto));
        BigDecimal total = repository.sumCantidadByProducto(idProducto);
        producto.setStock(total != null ? total : BigDecimal.ZERO);
        productoRepository.save(producto);
    }

    private StockProductoSector obtenerOCrear(Long idProducto, Long idSector) {
        return repository.findByProductoAndSector(idProducto, idSector)
                .orElseGet(() -> {
                    Producto producto = productoRepository.findById(idProducto)
                            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + idProducto));
                    Sector sector = sectorRepository.findById(idSector)
                            .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado con id: " + idSector));
                    return repository.save(StockProductoSector.builder()
                            .producto(producto)
                            .sector(sector)
                            .cantidad(BigDecimal.ZERO)
                            .build());
                });
    }
}
