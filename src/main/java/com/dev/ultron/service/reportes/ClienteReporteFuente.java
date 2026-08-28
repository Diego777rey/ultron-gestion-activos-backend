package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.personas.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class ClienteReporteFuente implements ReporteFuente {

    private final ClienteRepository clienteRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.CLIENTE;
    }

    @Override
    public String titulo() {
        return "Listado de clientes";
    }

    @Override
    public String subtitulo() {
        return "Personas y datos comerciales";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "TELÉFONO";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-clientes.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        List<Cliente> clientes = (filtro != null && !filtro.isBlank())
                ? clienteRepository.buscarParaReporte(filtro.trim())
                : clienteRepository.findAllParaReporte();
        return mapear(clientes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + id));
        return mapear(List.of(cliente));
    }

    private List<ReporteFila> mapear(List<Cliente> clientes) {
        AtomicInteger numero = new AtomicInteger(1);
        return clientes.stream()
                .map(cliente -> ReporteFilaMapper.deCliente(cliente, numero.getAndIncrement()))
                .toList();
    }
}
