package com.dev.ultron.service.impresion;

import com.dev.ultron.dto.impresion.input.TicketLineaInput;
import com.dev.ultron.dto.impresion.input.TicketVentaInput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Genera bytes ESC/POS para tickets de 58mm (32 caracteres).
 * No es el camino de impresión: el ticket se arma e imprime desde el frontend
 * ({@code escpos-ticket-builder.ts}). Esta clase queda solo como respaldo.
 */
public final class EscPosTicketBuilder {

    public static final int WIDTH_58MM = 32;
    /** Ancho imprimible de una térmica de 58 mm a 203 dpi. */
    private static final int DOTS_58MM = 384;
    private static final LogoRaster LOGO = loadLogo();
    private static final Charset IBM850 = Charset.forName("IBM850");
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat GS_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-PY"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        GS_FORMAT = new DecimalFormat("#,##0", symbols);
        GS_FORMAT.setGroupingUsed(true);
        GS_FORMAT.setMaximumFractionDigits(0);
    }

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final int width;

    public EscPosTicketBuilder() {
        this(WIDTH_58MM);
    }

    public EscPosTicketBuilder(int width) {
        this.width = width;
    }

    public EscPosTicketBuilder init() {
        write(new byte[]{0x1B, 0x40});
        write(new byte[]{0x1B, 0x74, 0x02});
        return this;
    }

    public EscPosTicketBuilder align(int n) {
        write(new byte[]{0x1B, 0x61, (byte) n});
        return this;
    }

    public EscPosTicketBuilder bold(boolean on) {
        write(new byte[]{0x1B, 0x45, (byte) (on ? 1 : 0)});
        return this;
    }

    public EscPosTicketBuilder line(String text) {
        writeText(sanitize(text));
        write(new byte[]{0x0A});
        return this;
    }

    /** Imprime el logo de CH Service centrado en el ancho de 58 mm. */
    public EscPosTicketBuilder logo() {
        return rasterCentered(LOGO.width, LOGO.height, LOGO.rows);
    }

    public EscPosTicketBuilder separator() {
        return line("-".repeat(width));
    }

    public EscPosTicketBuilder columns(String left, String right) {
        String l = sanitize(left);
        String r = sanitize(right);
        if (r.length() >= width) {
            return line(r.substring(r.length() - width));
        }
        int remaining = width - r.length();
        if (l.length() > remaining) {
            l = l.substring(0, remaining);
        }
        return line(l + " ".repeat(remaining - l.length()) + r);
    }

    public EscPosTicketBuilder feed(int lines) {
        write(new byte[]{0x1B, 0x64, (byte) Math.max(0, lines)});
        return this;
    }

    public EscPosTicketBuilder cut() {
        write(new byte[]{0x1D, 0x56, 0x41, 0x10});
        return this;
    }

    public byte[] toBytes() {
        return out.toByteArray();
    }

    public static byte[] prueba(String printerName) {
        return new EscPosTicketBuilder()
                .init()
                .align(1)
                .bold(true)
                .line("CH-SERVICE")
                .bold(false)
                .line("PRUEBA DE IMPRESION")
                .separator()
                .align(0)
                .line("Impresora: " + nullToEmpty(printerName))
                .line("Fecha: " + LocalDateTime.now().format(FECHA))
                .line("1234567890 ABCDEFGHIJK")
                .separator()
                .align(1)
                .line("Si lees esto, la impresora")
                .line("termica esta lista.")
                .feed(3)
                .cut()
                .toBytes();
    }

    public static byte[] ticketVenta(TicketVentaInput ticket) {
        EscPosTicketBuilder builder = new EscPosTicketBuilder().init().align(1);
        builder.logo();
        builder.feed(1);
        if (notBlank(ticket.getSubtitulo())) {
            builder.line(ticket.getSubtitulo());
        }
        builder.line("TICKET DE VENTA");
        builder.separator();
        builder.align(0);
        if (notBlank(ticket.getNumero())) {
            builder.line("Venta: " + ticket.getNumero());
        }
        builder.line("Fecha: " + blankTo(ticket.getFecha(), LocalDateTime.now().format(FECHA)));
        if (notBlank(ticket.getCajero())) {
            builder.line("Cajero: " + ticket.getCajero());
        }
        if (notBlank(ticket.getCliente())) {
            builder.line("Cliente: " + ticket.getCliente());
        }
        builder.separator();
        builder.line("Descripcion");
        builder.columns("Cant   P.U", "Total");
        builder.separator();

        List<TicketLineaInput> lineas = ticket.getLineas();
        if (lineas != null) {
            for (TicketLineaInput linea : lineas) {
                builder.line(blankTo(linea.getDescripcion(), "Item"));
                String cant = formatCantidad(linea.getCantidad());
                String pu = formatGs(linea.getPrecioUnitario());
                String total = formatGs(linea.getSubtotal() != null
                        ? linea.getSubtotal()
                        : multiply(linea.getCantidad(), linea.getPrecioUnitario()));
                builder.columns(cant + " x " + pu, total);
            }
        }

        builder.separator();
        if (ticket.getDescuento() != null && ticket.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
            builder.columns("Descuento", formatGs(ticket.getDescuento()));
        }
        builder.bold(true);
        builder.columns("TOTAL Gs.", formatGs(ticket.getTotal()));
        builder.bold(false);
        builder.separator();
        builder.align(1);
        builder.line(blankTo(ticket.getPie(), "Gracias por su compra"));
        builder.feed(3);
        builder.cut();
        return builder.toBytes();
    }

    public static String formatGs(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return GS_FORMAT.format(value);
    }

    static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('ñ', 'n')
                .replace('Ñ', 'N');
        return normalized.replaceAll("[^\\x20-\\x7E]", "?");
    }

    private EscPosTicketBuilder rasterCentered(int widthDots, int heightDots, byte[] rows) {
        int srcBytes = (widthDots + 7) / 8;
        int paperBytes = DOTS_58MM / 8;
        int left = Math.max(0, (paperBytes - srcBytes) / 2);
        int outBytes = Math.max(paperBytes, srcBytes);
        write(new byte[]{
                0x1D, 0x76, 0x30, 0x00,
                (byte) (outBytes & 0xFF),
                (byte) ((outBytes >> 8) & 0xFF),
                (byte) (heightDots & 0xFF),
                (byte) ((heightDots >> 8) & 0xFF)
        });
        byte[] row = new byte[outBytes];
        for (int y = 0; y < heightDots; y++) {
            Arrays.fill(row, (byte) 0);
            System.arraycopy(rows, y * srcBytes, row, left, srcBytes);
            write(row);
        }
        return this;
    }

    private void writeText(String text) {
        write(text.getBytes(IBM850));
    }

    private static LogoRaster loadLogo() {
        try (InputStream in = EscPosTicketBuilder.class.getResourceAsStream("/impresion/chservice-logo.bin")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el logo del ticket");
            }
            byte[] all = in.readAllBytes();
            if (all.length < 4) {
                throw new IllegalStateException("El logo del ticket está incompleto");
            }
            int width = ((all[0] & 0xFF) << 8) | (all[1] & 0xFF);
            int height = ((all[2] & 0xFF) << 8) | (all[3] & 0xFF);
            int rowBytes = (width + 7) / 8;
            byte[] rows = Arrays.copyOfRange(all, 4, all.length);
            if (width <= 0 || height <= 0 || rows.length != rowBytes * height) {
                throw new IllegalStateException("El logo del ticket no tiene el tamaño esperado");
            }
            return new LogoRaster(width, height, rows);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el logo del ticket", e);
        }
    }

    private record LogoRaster(int width, int height, byte[] rows) {
    }

    private void write(byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo armar el ticket", e);
        }
    }

    private static String formatCantidad(BigDecimal cantidad) {
        if (cantidad == null) {
            return "1";
        }
        if (cantidad.stripTrailingZeros().scale() <= 0) {
            return String.valueOf(cantidad.intValue());
        }
        return cantidad.stripTrailingZeros().toPlainString();
    }

    private static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        BigDecimal left = a != null ? a : BigDecimal.ONE;
        BigDecimal right = b != null ? b : BigDecimal.ZERO;
        return left.multiply(right);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankTo(String value, String fallback) {
        return notBlank(value) ? value.trim() : fallback;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
