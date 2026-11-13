package com.dev.ultron.service.web;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.ultron.domain.web.SuscripcionNewsletter;
import com.dev.ultron.dto.web.input.SuscripcionNewsletterInput;
import com.dev.ultron.dto.web.output.SuscripcionNewsletterOutput;
import com.dev.ultron.repository.web.SuscripcionNewsletterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuscripcionNewsletterService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final SuscripcionNewsletterRepository repository;

    @Transactional
    public SuscripcionNewsletterOutput suscribir(SuscripcionNewsletterInput input) {
        if (input == null || input.getEmail() == null || input.getEmail().isBlank()) {
            throw new IllegalArgumentException("Ingresá tu correo electrónico");
        }
        String email = input.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("El correo electrónico no es válido");
        }
        String nombre = input.getNombre() == null ? null : input.getNombre().trim();
        if (nombre != null && nombre.isEmpty()) {
            nombre = null;
        }

        SuscripcionNewsletter existente = repository.findByEmailIgnoreCase(email).orElse(null);
        if (existente != null) {
            existente.setActivo(true);
            if (nombre != null) {
                existente.setNombre(nombre);
            }
            SuscripcionNewsletter guardada = repository.save(existente);
            return toOutput(guardada, "Ya estabas suscripto. Te mantendremos al tanto de las ofertas.");
        }

        SuscripcionNewsletter nueva = SuscripcionNewsletter.builder()
                .email(email)
                .nombre(nombre)
                .activo(true)
                .build();
        SuscripcionNewsletter guardada = repository.save(nueva);
        return toOutput(guardada, "¡Listo! Te suscribiste al boletín de ofertas.");
    }

    private SuscripcionNewsletterOutput toOutput(SuscripcionNewsletter entidad, String mensaje) {
        SuscripcionNewsletterOutput output = new SuscripcionNewsletterOutput();
        output.setId_suscripcion(entidad.getId_suscripcion());
        output.setEmail(entidad.getEmail());
        output.setNombre(entidad.getNombre());
        output.setActivo(entidad.isActivo());
        output.setCreadoEn(entidad.getCreadoEn() == null ? null : entidad.getCreadoEn().toString());
        output.setMensaje(mensaje);
        return output;
    }
}
