package com.dev.ultron.service.impresion;

import com.dev.ultron.dto.impresion.input.TicketVentaInput;
import com.dev.ultron.dto.impresion.output.ImpresionResultado;
import com.dev.ultron.dto.impresion.output.ImpresoraOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio genérico de impresión térmica.
 * Usa la cola del sistema: CUPS en Linux/macOS y spooler en Windows.
 */
@Service
public class ImpresionService {

    private static final Logger log = LoggerFactory.getLogger(ImpresionService.class);

    public List<ImpresoraOutput> listarImpresoras() {
        PrintService[] services = PrinterJob.lookupPrintServices();
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        String defaultName = defaultService != null ? defaultService.getName() : null;

        List<ImpresoraOutput> result = new ArrayList<>();
        for (PrintService service : services) {
            String name = service.getName();
            result.add(ImpresoraOutput.builder()
                    .name(name)
                    .displayName(name)
                    .isDefault(defaultName != null && defaultName.equals(name))
                    .build());
        }
        return result;
    }

    public ImpresionResultado imprimirPrueba(String printerName) {
        return imprimirRaw(printerName, EscPosTicketBuilder.prueba(printerName), "Ticket de prueba enviado");
    }

    public ImpresionResultado imprimirTicketVenta(String printerName, TicketVentaInput ticket) {
        if (ticket == null || ticket.getLineas() == null || ticket.getLineas().isEmpty()) {
            return ImpresionResultado.error("El ticket no tiene líneas para imprimir");
        }
        return imprimirRaw(printerName, EscPosTicketBuilder.ticketVenta(ticket), "Ticket enviado a la impresora");
    }

    public ImpresionResultado imprimirRaw(String printerName, byte[] data, String okMessage) {
        if (printerName == null || printerName.isBlank()) {
            return ImpresionResultado.error("Indicá el nombre de la impresora térmica");
        }
        PrintService printService = buscarImpresora(printerName.trim());
        if (printService == null) {
            return ImpresionResultado.error(
                    "No se encontró la impresora \"" + printerName.trim()
                            + "\". En Linux revisá CUPS con lpstat -p; en Windows, la cola de impresión.");
        }
        try {
            enviar(printService, data);
            return ImpresionResultado.ok(okMessage + " (" + printService.getName() + ")");
        } catch (PrintException e) {
            log.error("Error al imprimir en {}", printService.getName(), e);
            return ImpresionResultado.error("No se pudo imprimir: " + e.getMessage());
        }
    }

    PrintService buscarImpresora(String printerName) {
        PrintService[] services = PrinterJob.lookupPrintServices();
        for (PrintService service : services) {
            if (service.getName().equals(printerName)) {
                return service;
            }
        }
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }
        for (PrintService service : services) {
            if (service.getName().toLowerCase().contains(printerName.toLowerCase())) {
                return service;
            }
        }
        return null;
    }

    private void enviar(PrintService printService, byte[] data) throws PrintException {
        DocPrintJob job = printService.createPrintJob();
        Doc doc = new SimpleDoc(data, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        job.print(doc, null);
    }
}
