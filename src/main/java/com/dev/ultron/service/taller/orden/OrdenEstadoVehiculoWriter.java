package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.taller.OrdenEstadoVehiculo;
import com.dev.ultron.dto.taller.input.OrdenEstadoVehiculoInput;

import org.springframework.stereotype.Component;

/**
 * Escribe la pieza de estado inicial del vehículo.
 */
@Component
public class OrdenEstadoVehiculoWriter {

    public void aplicar(OrdenEstadoVehiculo estado, OrdenEstadoVehiculoInput input) {
        if (input == null || estado == null) {
            return;
        }
        if (input.falla_mecanica() != null) {
            estado.setFallaMecanica(input.falla_mecanica());
        }
        if (input.falla_electrica() != null) {
            estado.setFallaElectrica(input.falla_electrica());
        }
        if (input.estado_llantas() != null) {
            estado.setEstadoLlantas(input.estado_llantas());
        }
        if (input.estado_pintura() != null) {
            estado.setEstadoPintura(input.estado_pintura());
        }
        if (input.estado_rayones() != null) {
            estado.setEstadoRayones(input.estado_rayones());
        }
        if (input.estado_golpes() != null) {
            estado.setEstadoGolpes(input.estado_golpes());
        }
        if (input.estado_vidrios() != null) {
            estado.setEstadoVidrios(input.estado_vidrios());
        }
        if (input.perdida_aceite() != null) {
            estado.setPerdidaAceite(input.perdida_aceite());
        }
        if (input.luces_danadas() != null) {
            estado.setLucesDanadas(input.luces_danadas());
        }
        if (input.espejos_danados() != null) {
            estado.setEspejosDanados(input.espejos_danados());
        }
        if (input.accesorios_faltantes() != null) {
            estado.setAccesoriosFaltantes(input.accesorios_faltantes());
        }
        if (input.nivel_combustible() != null) {
            String nivel = input.nivel_combustible().isBlank()
                    ? null
                    : input.nivel_combustible().trim().toUpperCase();
            estado.setNivelCombustible(nivel);
        }
        if (input.kilometraje() != null) {
            if (input.kilometraje() < 0) {
                throw new IllegalArgumentException("El kilometraje no puede ser negativo");
            }
            estado.setKilometraje(input.kilometraje());
        }
        if (input.observaciones_estado() != null) {
            estado.setObservacionesEstado(
                    input.observaciones_estado().isBlank()
                            ? null
                            : input.observaciones_estado().toUpperCase());
        }
    }
}
