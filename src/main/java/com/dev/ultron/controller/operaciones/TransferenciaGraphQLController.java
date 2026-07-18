package com.dev.ultron.controller.operaciones;

import com.dev.ultron.dto.operaciones.input.TransferenciaDetalleInput;
import com.dev.ultron.dto.operaciones.input.TransferenciaInput;
import com.dev.ultron.dto.operaciones.output.StockProductoSectorOutput;
import com.dev.ultron.dto.operaciones.output.TransferenciaOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.operaciones.StockProductoSectorService;
import com.dev.ultron.service.operaciones.TransferenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class TransferenciaGraphQLController {

    private final TransferenciaService transferenciaService;
    private final StockProductoSectorService stockService;

    @QueryMapping
    public PageResponse<TransferenciaOutput> listarTransferenciasPaginado(
            @Argument int page,
            @Argument int size,
            @Argument String filter
    ) {
        return transferenciaService.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public TransferenciaOutput buscarTransferenciaPorId(@Argument Long id) {
        return transferenciaService.findById(id);
    }

    @QueryMapping
    public BigDecimal stockPorProductoSector(@Argument Long idProducto, @Argument Long idSector) {
        return stockService.getCantidad(idProducto, idSector);
    }

    @QueryMapping
    public PageResponse<StockProductoSectorOutput> listarStockPorSector(
            @Argument Long idSector,
            @Argument int page,
            @Argument int size,
            @Argument String filter
    ) {
        return stockService.listarPorSector(idSector, page, size, filter);
    }

    @MutationMapping
    public TransferenciaOutput registrarTransferencia(@Argument TransferenciaInput input) {
        return transferenciaService.registrar(input);
    }

    @MutationMapping
    public TransferenciaOutput agregarProductoTransferencia(
            @Argument Long idTransferencia,
            @Argument TransferenciaDetalleInput input
    ) {
        return transferenciaService.agregarProducto(idTransferencia, input);
    }

    @MutationMapping
    public TransferenciaOutput eliminarProductoTransferencia(
            @Argument Long idTransferencia,
            @Argument Long idDetalle
    ) {
        return transferenciaService.eliminarProducto(idTransferencia, idDetalle);
    }

    @MutationMapping
    public TransferenciaOutput conferirTransferencia(@Argument Long id) {
        return transferenciaService.conferir(id);
    }

    @MutationMapping
    public TransferenciaOutput recepcionarTransferencia(@Argument Long id) {
        return transferenciaService.recepcionar(id);
    }
}
