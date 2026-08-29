package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.patrimonio.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class VehiculoReporteFuente implements ReporteFuente {

    private final VehiculoRepository vehiculoRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.VEHICULO;
    }

    @Override
    public String titulo() {
        return "Listado de vehículos";
    }

    @Override
    public String subtitulo() {
        return "Patrimonio asignado a clientes";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "CLIENTE";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-vehiculos.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        List<Vehiculo> vehiculos = (filtro != null && !filtro.isBlank())
                ? vehiculoRepository.buscarParaReporte(filtro.trim())
                : vehiculoRepository.findAllParaReporte();
        return mapear(vehiculos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado con id: " + id));
        return mapear(List.of(vehiculo));
    }

    private List<ReporteFila> mapear(List<Vehiculo> vehiculos) {
        AtomicInteger numero = new AtomicInteger(1);
        return vehiculos.stream()
                .map(vehiculo -> ReporteFilaMapper.deVehiculo(vehiculo, numero.getAndIncrement()))
                .toList();
    }
}
