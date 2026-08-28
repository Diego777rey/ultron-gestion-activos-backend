package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.personas.Funcionario;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.personas.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class FuncionarioReporteFuente implements ReporteFuente {

    private final FuncionarioRepository funcionarioRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.FUNCIONARIO;
    }

    @Override
    public String titulo() {
        return "Listado de funcionarios";
    }

    @Override
    public String subtitulo() {
        return "Personal del taller";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "INGRESO";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-funcionarios.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        List<Funcionario> funcionarios = (filtro != null && !filtro.isBlank())
                ? funcionarioRepository.buscarParaReporte(filtro.trim())
                : funcionarioRepository.findAllParaReporte();
        return mapear(funcionarios);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        Funcionario funcionario = funcionarioRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionario no encontrado con id: " + id));
        return mapear(List.of(funcionario));
    }

    private List<ReporteFila> mapear(List<Funcionario> funcionarios) {
        AtomicInteger numero = new AtomicInteger(1);
        return funcionarios.stream()
                .map(funcionario -> ReporteFilaMapper.deFuncionario(funcionario, numero.getAndIncrement()))
                .toList();
    }
}
