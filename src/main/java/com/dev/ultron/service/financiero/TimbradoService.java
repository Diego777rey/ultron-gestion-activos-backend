package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.Timbrado;
import com.dev.ultron.domain.personas.Empresa;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.financiero.input.TimbradoInput;
import com.dev.ultron.dto.financiero.mapper.TimbradoMapper;
import com.dev.ultron.dto.financiero.output.TimbradoOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.repository.financiero.TimbradoRepository;
import com.dev.ultron.repository.personas.EmpresaRepository;
import com.dev.ultron.service.security.AuthService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de timbrados para facturación.
 */
@Service
public class TimbradoService extends GenericCrudService<Timbrado, Long> {

    private final TimbradoRepository timbradoRepository;
    private final EmpresaRepository empresaRepository;
    private final TimbradoMapper timbradoMapper;
    private final AuthService authService;

    public TimbradoService(TimbradoRepository timbradoRepository,
                          EmpresaRepository empresaRepository,
                          TimbradoMapper timbradoMapper,
                          AuthService authService) {
        this.timbradoRepository = timbradoRepository;
        this.empresaRepository = empresaRepository;
        this.timbradoMapper = timbradoMapper;
        this.authService = authService;
    }

    @Override
    protected JpaRepository<Timbrado, Long> getRepository() {
        return timbradoRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Timbrado timbrado) {
        if (timbrado.getNumero_timbrado() == null || timbrado.getNumero_timbrado().isEmpty()) {
            throw new IllegalArgumentException("El número de timbrado es obligatorio");
        }
        
        if (timbrado.getEstablecimiento() == null || timbrado.getEstablecimiento().isEmpty()) {
            throw new IllegalArgumentException("El establecimiento es obligatorio");
        }
        
        if (timbrado.getPunto_expedicion() == null || timbrado.getPunto_expedicion().isEmpty()) {
            throw new IllegalArgumentException("El punto de expedición es obligatorio");
        }

        timbrado.setEstablecimiento(normalizarCodigoPunto(timbrado.getEstablecimiento(), "establecimiento"));
        timbrado.setPunto_expedicion(normalizarCodigoPunto(timbrado.getPunto_expedicion(), "punto de expedición"));

        if (timbrado.getTipo_factura() == null || timbrado.getTipo_factura().isBlank()) {
            timbrado.setTipo_factura("PAPEL");
        }
        
        if (timbrado.getNumero_inicial() == null || timbrado.getNumero_final() == null) {
            throw new IllegalArgumentException("El rango de numeración es obligatorio");
        }
        
        if (timbrado.getNumero_inicial() > timbrado.getNumero_final()) {
            throw new IllegalArgumentException("El número inicial no puede ser mayor que el número final");
        }

        Integer actual = timbrado.getNumero_actual();
        if (actual != null && (actual < timbrado.getNumero_inicial() || actual > timbrado.getNumero_final() + 1)) {
            throw new IllegalArgumentException("El próximo número tiene que estar dentro del rango autorizado");
        }
        
        if (timbrado.getFecha_inicio_vigencia() == null || timbrado.getFecha_fin_vigencia() == null) {
            throw new IllegalArgumentException("Las fechas de vigencia son obligatorias");
        }
        
        if (timbrado.getFecha_inicio_vigencia().isAfter(timbrado.getFecha_fin_vigencia())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        boolean existe = timbrado.getId_timbrado() == null
                ? timbradoRepository.existsByNumeroAndEstablecimientoAndPunto(
                        timbrado.getNumero_timbrado(),
                        timbrado.getEstablecimiento(),
                        timbrado.getPunto_expedicion())
                : timbradoRepository.existsOtroConMismoPunto(
                        timbrado.getNumero_timbrado(),
                        timbrado.getEstablecimiento(),
                        timbrado.getPunto_expedicion(),
                        timbrado.getId_timbrado());
        
        if (existe) {
            throw new IllegalArgumentException(
                    "Ya existe un timbrado con el mismo número, establecimiento y punto de expedición"
            );
        }
    }

    /**
     * Registra un nuevo timbrado.
     */
    @Transactional
    public TimbradoOutput registrarTimbrado(TimbradoInput input) {
        Empresa empresa = empresaRepository.findById(input.idEmpresa())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        Usuario usuarioCreador = authService.getAuthenticatedUser();
        
        Timbrado timbrado = timbradoMapper.toEntity(input, empresa, usuarioCreador);
        
        if (timbrado.getNumero_actual() == null) {
            timbrado.setNumero_actual(timbrado.getNumero_inicial());
        }
        
        timbrado = guardar(timbrado);
        
        return timbradoMapper.toOutput(timbrado);
    }

    /**
     * Actualiza un timbrado existente.
     */
    @Transactional
    public TimbradoOutput actualizarTimbrado(Long id, TimbradoInput input) {
        Timbrado timbrado = buscarPorIdOrThrow(id);
        
        Integer numeroActualOriginal = timbrado.getNumero_actual();
        
        timbradoMapper.updateEntity(timbrado, input);
        
        if (timbrado.getNumero_actual() == null) {
            timbrado.setNumero_actual(numeroActualOriginal);
        }
        
        timbrado = actualizar(timbrado);
        
        return timbradoMapper.toOutput(timbrado);
    }

    /**
     * Obtiene el timbrado activo y disponible para una empresa.
     */
    @Transactional(readOnly = true)
    public TimbradoOutput obtenerTimbradoDisponible(Long idEmpresa) {
        Timbrado timbrado = timbradoRepository.findTimbradoActivoDisponible(idEmpresa)
                .orElseThrow(() -> new IllegalStateException(
                        "No hay timbrado activo y vigente disponible para esta empresa"
                ));
        
        return timbradoMapper.toOutput(timbrado);
    }

    /**
     * Lista todos los timbrados de una empresa.
     */
    @Transactional(readOnly = true)
    public List<TimbradoOutput> listarPorEmpresa(Long idEmpresa) {
        return timbradoRepository.findAllByEmpresa(idEmpresa)
                .stream()
                .map(timbradoMapper::toOutput)
                .collect(Collectors.toList());
    }

    /**
     * Lista los timbrados activos de una empresa.
     */
    @Transactional(readOnly = true)
    public List<TimbradoOutput> listarActivosPorEmpresa(Long idEmpresa) {
        return timbradoRepository.findActivosByEmpresa(idEmpresa)
                .stream()
                .map(timbradoMapper::toOutput)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un timbrado está vigente.
     */
    @Transactional(readOnly = true)
    public boolean estaVigente(Long id) {
        Timbrado timbrado = buscarPorIdOrThrow(id);
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(timbrado.getFecha_inicio_vigencia()) 
                && !hoy.isAfter(timbrado.getFecha_fin_vigencia());
    }

    /**
     * Verifica si un timbrado tiene números disponibles.
     */
    @Transactional(readOnly = true)
    public boolean tieneNumerosDisponibles(Long id) {
        Timbrado timbrado = buscarPorIdOrThrow(id);
        return timbrado.getNumero_actual() <= timbrado.getNumero_final();
    }

    /**
     * Desactiva un timbrado.
     */
    @Transactional
    public TimbradoOutput desactivar(Long id) {
        Timbrado timbrado = buscarPorIdOrThrow(id);
        timbrado.setActivo(false);
        timbrado = actualizar(timbrado);
        return timbradoMapper.toOutput(timbrado);
    }

    /**
     * Activa un timbrado.
     */
    @Transactional
    public TimbradoOutput activar(Long id) {
        Timbrado timbrado = buscarPorIdOrThrow(id);
        timbrado.setActivo(true);
        timbrado = actualizar(timbrado);
        return timbradoMapper.toOutput(timbrado);
    }

    private static String normalizarCodigoPunto(String valor, String campo) {
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.isEmpty() || digitos.length() > 3) {
            throw new IllegalArgumentException("El " + campo + " debe tener de 1 a 3 dígitos");
        }
        return String.format("%03d", Integer.parseInt(digitos));
    }

    /**
     * Obtiene un timbrado por ID y lo mapea a Output.
     */
    @Transactional(readOnly = true)
    public TimbradoOutput obtenerPorId(Long id) {
        Timbrado timbrado = buscarPorIdOrThrow(id);
        return timbradoMapper.toOutput(timbrado);
    }
}
