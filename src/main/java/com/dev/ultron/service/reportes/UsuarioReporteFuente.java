package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.personas.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class UsuarioReporteFuente implements ReporteFuente {

    private final UsuarioRepository usuarioRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.USUARIO;
    }

    @Override
    public String titulo() {
        return "Listado de usuarios";
    }

    @Override
    public String subtitulo() {
        return "Cuentas de acceso al sistema";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "ROLES";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-usuarios.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        List<Usuario> usuarios = (filtro != null && !filtro.isBlank())
                ? usuarioRepository.buscarParaReporte(filtro.trim())
                : usuarioRepository.findAllWithRolesAndFuncionario();
        return mapear(usuarios);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findByIdWithRolesAndFuncionario(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        return mapear(List.of(usuario));
    }

    private List<ReporteFila> mapear(List<Usuario> usuarios) {
        AtomicInteger numero = new AtomicInteger(1);
        return usuarios.stream()
                .map(usuario -> ReporteFilaMapper.deUsuario(usuario, numero.getAndIncrement()))
                .toList();
    }
}
