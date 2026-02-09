package com.dev.ultron.service;

import com.dev.ultron.domain.patrimonio.Bien;
import com.dev.ultron.domain.patrimonio.PropiedadBien;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.BienConPorcentajeInput;
import com.dev.ultron.dto.personas.input.RegistroPersonaConBienesInput;
import com.dev.ultron.repository.patrimonio.BienRepository;
import com.dev.ultron.repository.patrimonio.PropiedadBienRepository;
import com.dev.ultron.repository.personas.PersonaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final BienRepository bienRepository;
    private final PropiedadBienRepository propiedadBienRepository;

    @Transactional
    public Persona registrarPersonaConBienes(RegistroPersonaConBienesInput input) {
        // 1. Guardar Persona
        var pesonaAGuardar = Persona.builder()
                .nombre(input.persona().nombre())
                .apellido(input.persona().apellido())
                .documento(input.persona().documento())
                .email(input.persona().email())
                .telefono(input.persona().telefono())
                .estado(input.persona().estado())
                .build();

        var personaGuardada = personaRepository.save(pesonaAGuardar);

        // 2. Guardar Bienes y Relacionar
        if (input.bienes() != null && !input.bienes().isEmpty()) {
            for (BienConPorcentajeInput bienInput : input.bienes()) {
                // Crear y guardar el Bien
                var bien = Bien.builder()
                        .tipo(bienInput.bien().tipo())
                        .descripcion(bienInput.bien().descripcion())
                        .valor(bienInput.bien().valor())
                        .fecha_adquisicion(bienInput.bien().fecha_adquisicion())
                        .id_empresa(bienInput.bien().id_empresa())
                        .estado(bienInput.bien().estado())
                        .build();

                var bienGuardado = bienRepository.save(bien);

                // Crear relacion PropiedadBien
                var propiedad = PropiedadBien.builder()
                        .persona(personaGuardada)
                        .bien(bienGuardado)
                        .porcentaje_propiedad(bienInput.porcentaje())
                        .build();

                propiedadBienRepository.save(propiedad);
            }
        }

        return personaGuardada;
    }
}
