package com.dev.ultron.service.impresion;

import com.dev.ultron.dto.impresion.input.TicketLineaInput;
import com.dev.ultron.dto.impresion.input.TicketVentaInput;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EscPosTicketBuilderTest {

    private static final Charset IBM850 = Charset.forName("IBM850");

    @Test
    void pruebaIncluyeEncabezadoYCorte() {
        byte[] bytes = EscPosTicketBuilder.prueba("TICKET58");
        String text = new String(bytes, IBM850);

        assertTrue(text.contains("CH-SERVICE"));
        assertTrue(text.contains("PRUEBA DE IMPRESION"));
        assertTrue(text.contains("TICKET58"));
        assertTrue(containsCut(bytes));
        assertTrue(bytes.length > 32);
    }

    @Test
    void ticketVentaIncluyeLineasYTotal() {
        TicketVentaInput ticket = TicketVentaInput.builder()
                .titulo("CH-SERVICE")
                .subtitulo(null)
                .numero("VEN-20260921-1-0001")
                .fecha("21/09/2026 15:51")
                .cajero("ADMIN")
                .cliente("Consumidor final")
                .lineas(List.of(
                        TicketLineaInput.builder()
                                .descripcion("LIQUIDO DE FRENOS DOT 4")
                                .cantidad(BigDecimal.ONE)
                                .precioUnitario(new BigDecimal("42000"))
                                .subtotal(new BigDecimal("42000"))
                                .build(),
                        TicketLineaInput.builder()
                                .descripcion("BATERIA 12V")
                                .cantidad(new BigDecimal("2"))
                                .precioUnitario(new BigDecimal("560000"))
                                .subtotal(new BigDecimal("1120000"))
                                .build()
                ))
                .descuento(BigDecimal.ZERO)
                .total(new BigDecimal("1162000"))
                .pie("Gracias por su compra")
                .build();

        String text = new String(EscPosTicketBuilder.ticketVenta(ticket), IBM850);

        assertTrue(text.contains("TICKET DE VENTA"));
        assertTrue(text.contains("VEN-20260921-1-0001"));
        assertTrue(text.contains("LIQUIDO DE FRENOS DOT 4"));
        assertTrue(text.contains("BATERIA 12V"));
        assertTrue(text.contains("1.162.000"));
        assertTrue(text.contains("Gracias por su compra"));
        assertFalse(text.contains("á"));
    }

    @Test
    void sanitizeQuitaAcentos() {
        assertTrue(EscPosTicketBuilder.sanitize("Gestión térmica ñ").equals("Gestion termica n"));
    }

    private static boolean containsCut(byte[] bytes) {
        for (int i = 0; i < bytes.length - 3; i++) {
            if (bytes[i] == 0x1D && bytes[i + 1] == 0x56 && bytes[i + 2] == 0x41) {
                return true;
            }
        }
        return false;
    }
}
