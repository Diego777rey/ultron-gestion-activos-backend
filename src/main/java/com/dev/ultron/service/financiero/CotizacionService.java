package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Cotizacion;
import com.dev.ultron.dto.financiero.input.CotizacionInput;
import com.dev.ultron.dto.financiero.mapper.CotizacionMapper;
import com.dev.ultron.dto.financiero.output.CotizacionOutput;
import com.dev.ultron.dto.financiero.output.MontoCotizadoOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.CotizacionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CotizacionService extends GenericCrudService<Cotizacion, Long> {

    private static final int MONEDA_MAX_LENGTH = 80;

    private final CotizacionRepository repository;
    private final CotizacionMapper mapper;

    public CotizacionService(
            CotizacionRepository repository,
            CotizacionMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    protected JpaRepository<Cotizacion, Long> getRepository() {
        return repository;
    }

    @Transactional
    public CotizacionOutput save(CotizacionInput input) {
        normalizarMoneda(input);
        Cotizacion entidad = mapper.toEntity(input);
        entidad.setFechaActualizacion(LocalDateTime.now());
        if (entidad.getActiva() == null) {
            entidad.setActiva(true);
        }
        return mapper.toOutput(guardar(entidad));
    }

    @Transactional
    public CotizacionOutput update(Long id, CotizacionInput input) {
        normalizarMoneda(input);
        Cotizacion entidad = buscarPorIdOrThrow(id);
        mapper.updateEntity(entidad, input);
        entidad.setFechaActualizacion(LocalDateTime.now());
        return mapper.toOutput(actualizar(entidad));
    }

    @Transactional(readOnly = true)
    public List<CotizacionOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CotizacionOutput> findAllActivas() {
        return repository.findAllActivas().stream()
                .map(mapper::toOutput)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Cotizacion buscarActiva(String moneda) {
        if (moneda == null || moneda.isBlank()) {
            return null;
        }
        return repository.findActivaByMoneda(moneda.trim());
    }

    /**
     * Convierte un total en guaraníes a la moneda de la cotización.
     * {@code valorCotizacion} es la cantidad de guaraníes por una unidad de esa moneda.
     */
    public BigDecimal convertirDesdePyg(BigDecimal totalPyg, BigDecimal valorCotizacion) {
        if (totalPyg == null) {
            throw new IllegalArgumentException("Debe indicar el total en guaraníes");
        }
        if (valorCotizacion == null || valorCotizacion.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cotización debe ser mayor a cero");
        }
        return totalPyg.divide(valorCotizacion, 2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<MontoCotizadoOutput> cotizarTotal(BigDecimal totalPyg) {
        if (totalPyg == null || totalPyg.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total en guaraníes debe ser mayor o igual a cero");
        }
        List<MontoCotizadoOutput> montos = new ArrayList<>();
        montos.add(MontoCotizadoOutput.builder()
                .moneda("PYG")
                .monto(totalPyg)
                .build());
        for (Cotizacion cotizacion : repository.findAllActivas()) {
            if (cotizacion.getValor() == null || cotizacion.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            montos.add(MontoCotizadoOutput.builder()
                    .moneda(cotizacion.getMoneda())
                    .valorCotizacion(cotizacion.getValor())
                    .monto(convertirDesdePyg(totalPyg, cotizacion.getValor()))
                    .build());
        }
        return montos;
    }

    @Transactional(readOnly = true)
    public PageResponse<CotizacionOutput> findAllPaginated(int page, int size, String filter) {
        return new PageResponse<>(repository.buscar(filter, PageRequest.of(page, size)).map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public CotizacionOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }

    private void normalizarMoneda(CotizacionInput input) {
        String moneda = input.getMoneda() == null ? "" : input.getMoneda().trim();
        if (moneda.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar la moneda");
        }
        if (moneda.length() > MONEDA_MAX_LENGTH) {
            throw new IllegalArgumentException("La moneda no puede superar 80 caracteres");
        }
        input.setMoneda(moneda);
    }
}
