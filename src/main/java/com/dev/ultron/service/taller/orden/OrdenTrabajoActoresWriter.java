package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Funcionario;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.dto.taller.input.OrdenTrabajoInput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.personas.FuncionarioRepository;
import com.dev.ultron.repository.personas.UsuarioRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import com.dev.ultron.service.patrimonio.VehiculoService;
import com.dev.ultron.service.personas.ClienteService;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Enlaza actores y relaciones del core de la orden (sector, responsable, cliente, vehículo, mecánico, caja).
 */
@Component
public class OrdenTrabajoActoresWriter {

    private final ClienteService clienteService;
    private final VehiculoService vehiculoService;
    private final FuncionarioRepository funcionarioRepo;
    private final SectorRepository sectorRepo;
    private final UsuarioRepository usuarioRepo;
    private final OrdenTrabajoCajaResolver cajaResolver;

    public OrdenTrabajoActoresWriter(
            ClienteService clienteService,
            VehiculoService vehiculoService,
            FuncionarioRepository funcionarioRepo,
            SectorRepository sectorRepo,
            UsuarioRepository usuarioRepo,
            OrdenTrabajoCajaResolver cajaResolver) {
        this.clienteService = clienteService;
        this.vehiculoService = vehiculoService;
        this.funcionarioRepo = funcionarioRepo;
        this.sectorRepo = sectorRepo;
        this.usuarioRepo = usuarioRepo;
        this.cajaResolver = cajaResolver;
    }

    public void aplicar(OrdenTrabajo orden, OrdenTrabajoInput input, boolean creando) {
        if (input.id_sector() != null) {
            Sector sector = sectorRepo.findById(input.id_sector())
                    .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado: " + input.id_sector()));
            orden.setSector(sector);
        }
        if (input.id_responsable() != null) {
            Usuario responsable = usuarioRepo.findById(input.id_responsable())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Usuario responsable no encontrado: " + input.id_responsable()));
            orden.setResponsable(responsable);
        }
        if (input.id_cliente() != null) {
            orden.setCliente(clienteService.buscarPorIdOrThrow(input.id_cliente()));
        }
        if (input.id_vehiculo() != null) {
            orden.setVehiculo(vehiculoService.buscarPorIdOrThrow(input.id_vehiculo()));
        }
        aplicarMecanicos(orden, input, creando);
        if (input.id_caja() != null) {
            orden.setCaja(cajaResolver.exigirConSesionAbierta(input.id_caja()));
        }

        if (creando || input.id_cliente() != null || input.id_vehiculo() != null) {
            validarVehiculoPerteneceACliente(orden.getCliente(), orden.getVehiculo());
        }
    }

    private void aplicarMecanicos(OrdenTrabajo orden, OrdenTrabajoInput input, boolean creando) {
        List<Long> ids = idsMecanicos(input);
        if (ids == null) {
            if (creando) {
                throw new IllegalArgumentException("Debes asignar al menos un mecánico");
            }
            return;
        }
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("Debes asignar al menos un mecánico");
        }

        List<Funcionario> asignados = new ArrayList<>();
        Set<Long> vistos = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id == null || !vistos.add(id)) {
                continue;
            }
            Funcionario mecanico = funcionarioRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado: " + id));
            asignados.add(mecanico);
        }
        if (asignados.isEmpty()) {
            throw new IllegalArgumentException("Debes asignar al menos un mecánico");
        }

        if (!creando) {
            validarMecanicosConServicios(orden, vistos);
        }

        if (orden.getMecanicos() == null) {
            orden.setMecanicos(new ArrayList<>());
        }
        orden.getMecanicos().clear();
        orden.getMecanicos().addAll(asignados);
        orden.setMecanico(asignados.get(0));
    }

    private List<Long> idsMecanicos(OrdenTrabajoInput input) {
        if (input.ids_mecanicos() != null) {
            return input.ids_mecanicos();
        }
        if (input.id_mecanico() != null) {
            return List.of(input.id_mecanico());
        }
        return null;
    }

    private void validarMecanicosConServicios(OrdenTrabajo orden, Set<Long> nuevosIds) {
        if (orden.getDetalles() == null) {
            return;
        }
        for (var detalle : orden.getDetalles()) {
            if (detalle.getMecanico() == null || detalle.getMecanico().getId_funcionario() == null) {
                continue;
            }
            Long id = detalle.getMecanico().getId_funcionario();
            if (!nuevosIds.contains(id)) {
                throw new IllegalArgumentException(
                        "No se puede quitar un mecánico que ya tiene servicios asignados en el presupuesto");
            }
        }
    }

    public void validarVehiculoPerteneceACliente(Cliente cliente, Vehiculo vehiculo) {
        if (cliente == null || vehiculo == null) {
            return;
        }
        if (vehiculo.getCliente() == null
                || !Objects.equals(vehiculo.getCliente().getId_cliente(), cliente.getId_cliente())) {
            throw new IllegalArgumentException("El vehículo no pertenece al cliente seleccionado");
        }
    }
}
