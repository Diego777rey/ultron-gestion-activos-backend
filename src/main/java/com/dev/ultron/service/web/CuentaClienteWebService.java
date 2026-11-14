package com.dev.ultron.service.web;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.web.CuentaClienteWeb;
import com.dev.ultron.dto.personas.input.ClienteInput;
import com.dev.ultron.dto.personas.input.PersonaInput;
import com.dev.ultron.dto.personas.output.ClienteOutput;
import com.dev.ultron.dto.web.input.LoginClienteWebInput;
import com.dev.ultron.dto.web.input.RegistroClienteWebInput;
import com.dev.ultron.dto.web.output.SesionClienteWebOutput;
import com.dev.ultron.repository.web.CuentaClienteWebRepository;
import com.dev.ultron.service.personas.ClienteService;
import com.dev.ultron.service.security.TokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CuentaClienteWebService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final CuentaClienteWebRepository cuentaRepository;
    private final ClienteService clienteService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public SesionClienteWebOutput registrar(RegistroClienteWebInput input) {
        validarRegistro(input);
        String email = normalizarEmail(input.getEmail());
        if (cuentaRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo electrónico");
        }

        String documento = input.getDocumento().trim();
        String ruc = trimToNull(input.getRuc());
        if (ruc == null) {
            ruc = documento;
        }

        PersonaInput persona = new PersonaInput(
                input.getNombre().trim(),
                trimToNull(input.getApellido()),
                documento,
                email,
                trimToNull(input.getTelefono()),
                trimToNull(input.getDireccion()),
                "ACTIVO");

        ClienteInput clienteInput = new ClienteInput(
                persona,
                ruc,
                "PERSONA FISICA",
                null,
                LocalDate.now().toString(),
                "Registro tienda web",
                true);

        ClienteOutput clienteCreado;
        try {
            clienteCreado = clienteService.registrarCliente(clienteInput);
        } catch (IllegalArgumentException ex) {
            // Si ya es cliente en el ERP, vinculamos cuenta a ese cliente.
            if (ex.getMessage() != null && ex.getMessage().contains("Ya existe un cliente")) {
                Cliente existente = clienteService.buscarEntidadPorDocumento(documento)
                        .orElseThrow(() -> ex);
                if (cuentaRepository.findByClienteId(existente.getId_cliente()).isPresent()) {
                    throw new IllegalArgumentException(
                            "Este documento ya tiene una cuenta en la tienda. Iniciá sesión.");
                }
                return crearCuentaYSesion(existente, email, input.getPassword(),
                        "Cuenta creada y vinculada a tu ficha de cliente.");
            }
            throw ex;
        }

        Cliente cliente = clienteService.buscarPorIdOrThrow(clienteCreado.getId_cliente());
        return crearCuentaYSesion(cliente, email, input.getPassword(),
                "¡Bienvenido! Tu cuenta quedó registrada.");
    }

    @Transactional(readOnly = true)
    public SesionClienteWebOutput login(LoginClienteWebInput input) {
        if (input == null || input.getEmail() == null || input.getPassword() == null) {
            throw new IllegalArgumentException("Ingresá tu correo y contraseña");
        }
        String email = normalizarEmail(input.getEmail());
        CuentaClienteWeb cuenta = cuentaRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Correo o contraseña incorrectos"));
        if (!cuenta.isActivo()) {
            throw new IllegalArgumentException("Tu cuenta está desactivada. Contactá a la tienda.");
        }
        if (!passwordEncoder.matches(input.getPassword(), cuenta.getPasswordHash())) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos");
        }
        return toSesion(cuenta, "Sesión iniciada.");
    }

    @Transactional(readOnly = true)
    public SesionClienteWebOutput miCuenta(Jwt jwt) {
        Long clienteId = resolverClienteId(jwt);
        CuentaClienteWeb cuenta = cuentaRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No encontramos tu cuenta de tienda"));
        return toSesion(cuenta, null);
    }

    public Long resolverClienteId(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("Tenés que iniciar sesión");
        }
        Object tipo = jwt.getClaim("tipo");
        if (!"CLIENTE_WEB".equals(tipo)) {
            throw new IllegalArgumentException("La sesión no corresponde a un cliente de la tienda");
        }
        Object id = jwt.getClaim("clienteId");
        if (id instanceof Number number) {
            return number.longValue();
        }
        if (id != null) {
            return Long.valueOf(id.toString());
        }
        throw new IllegalArgumentException("Sesión de cliente inválida");
    }

    private SesionClienteWebOutput crearCuentaYSesion(Cliente cliente, String email, String password, String mensaje) {
        CuentaClienteWeb cuenta = CuentaClienteWeb.builder()
                .cliente(cliente)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .activo(true)
                .build();
        cuenta = cuentaRepository.save(cuenta);
        return toSesion(cuenta, mensaje);
    }

    private SesionClienteWebOutput toSesion(CuentaClienteWeb cuenta, String mensaje) {
        Cliente cliente = cuenta.getCliente();
        String token = tokenService.generateClienteWebToken(cuenta.getEmail(), cliente.getId_cliente());
        SesionClienteWebOutput output = new SesionClienteWebOutput();
        output.setToken(token);
        output.setIdCliente(cliente.getId_cliente());
        output.setIdCuenta(cuenta.getId_cuenta_cliente());
        output.setEmail(cuenta.getEmail());
        if (cliente.getPersona() != null) {
            output.setNombre(cliente.getPersona().getNombre());
            output.setApellido(cliente.getPersona().getApellido());
            output.setDocumento(cliente.getPersona().getDocumento());
            output.setTelefono(cliente.getPersona().getTelefono());
            output.setDireccion(cliente.getPersona().getDireccion());
        }
        output.setRuc(cliente.getRuc());
        output.setMensaje(mensaje);
        return output;
    }

    private void validarRegistro(RegistroClienteWebInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Completá el formulario de registro");
        }
        if (input.getNombre() == null || input.getNombre().trim().length() < 2) {
            throw new IllegalArgumentException("Ingresá tu nombre");
        }
        if (input.getDocumento() == null || input.getDocumento().trim().length() < 4) {
            throw new IllegalArgumentException("Ingresá tu número de documento (CI)");
        }
        String email = normalizarEmail(input.getEmail());
        if (email.isEmpty() || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Ingresá un correo electrónico válido");
        }
        if (input.getPassword() == null || input.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (input.getPasswordConfirmacion() == null
                || !input.getPassword().equals(input.getPasswordConfirmacion())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
    }

    private static String normalizarEmail(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String t = valor.trim();
        return t.isEmpty() ? null : t;
    }
}
