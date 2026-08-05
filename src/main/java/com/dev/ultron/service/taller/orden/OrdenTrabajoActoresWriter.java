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

import java.util.Objects;

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
        if (input.id_mecanico() != null) {
            Funcionario mecanico = funcionarioRepo.findById(input.id_mecanico())
                    .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado: " + input.id_mecanico()));
            orden.setMecanico(mecanico);
        }
        if (input.id_caja() != null) {
            orden.setCaja(cajaResolver.exigirConSesionAbierta(input.id_caja()));
        }

        if (creando || input.id_cliente() != null || input.id_vehiculo() != null) {
            validarVehiculoPerteneceACliente(orden.getCliente(), orden.getVehiculo());
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
