package com.dev.ultron.service.common;

import com.dev.ultron.config.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    @TempDir
    Path uploadDir;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setUploadDir(uploadDir.toString());
        service = new FileStorageService(properties);
    }

    @Test
    void guardaLaImagenEnLaCarpetaDelServidorYDevuelveLaRutaRelativa() {
        String ruta = service.storeFile(imagen("foto.JPG"), "productos");

        assertTrue(ruta.matches("productos/[0-9a-f-]{36}\\.jpg"));
        assertTrue(Files.exists(uploadDir.resolve(ruta)));
        assertEquals(uploadDir.toAbsolutePath().normalize(), service.getStorageLocation());
    }

    @Test
    void noPermiteGuardarDentroDeLaCarpetaDeLaAplicacion() {
        FileStorageProperties relativa = new FileStorageProperties();
        relativa.setUploadDir("uploads");
        assertThrows(IllegalStateException.class, () -> new FileStorageService(relativa));

        FileStorageProperties dentro = new FileStorageProperties();
        dentro.setUploadDir(uploadDir.resolve("app/uploads").toString());
        assertThrows(IllegalStateException.class, () -> new FileStorageService(dentro, uploadDir.resolve("app")));
    }

    @Test
    void rechazaCarpetasQueSalenDelAlmacenamiento() {
        assertThrows(IllegalArgumentException.class, () -> service.storeFile(imagen("foto.png"), "../fuera"));
        assertThrows(IllegalArgumentException.class, () -> service.storeFile(imagen("foto.png"), "a/b"));
    }

    @Test
    void rechazaArchivosQueNoSonImagenes() {
        assertThrows(IllegalArgumentException.class, () -> service.storeFile(imagen("script.html"), "productos"));
        assertThrows(IllegalArgumentException.class, () -> service.storeFile(imagen("sin-extension"), "productos"));
    }

    @Test
    void eliminaSoloArchivosDentroDelAlmacenamiento() throws Exception {
        String ruta = service.storeFile(imagen("foto.webp"), "productos");
        service.deleteFile(ruta);
        assertFalse(Files.exists(uploadDir.resolve(ruta)));

        Path ajeno = Files.createTempFile("ultron-ajeno", ".jar");
        try {
            String rutaAjena = uploadDir.relativize(ajeno).toString();
            assertThrows(IllegalArgumentException.class, () -> service.deleteFile(rutaAjena));
            assertTrue(Files.exists(ajeno));
        } finally {
            Files.deleteIfExists(ajeno);
        }
    }

    private static MockMultipartFile imagen(String nombre) {
        return new MockMultipartFile("file", nombre, "image/jpeg", new byte[]{1, 2, 3});
    }
}
