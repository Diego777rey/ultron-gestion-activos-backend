package com.dev.ultron.service.common;

import com.dev.ultron.config.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FileStorageService {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final Pattern CARPETA_VALIDA = Pattern.compile("[a-z0-9_-]+");

    private final Path fileStorageLocation;
    private final FileStorageProperties fileStorageProperties;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this(fileStorageProperties, Paths.get(System.getProperty("user.dir")));
    }

    FileStorageService(FileStorageProperties fileStorageProperties, Path directorioAplicacion) {
        this.fileStorageProperties = fileStorageProperties;
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        Path aplicacion = directorioAplicacion.toAbsolutePath().normalize();
        if (this.fileStorageLocation.startsWith(aplicacion)) {
            throw new IllegalStateException("La carpeta de imágenes (" + this.fileStorageLocation
                    + ") no puede estar dentro de la carpeta de la aplicación (" + aplicacion
                    + "). Configurá ULTRON_UPLOAD_DIR con una ruta absoluta fuera del proyecto,"
                    + " por ejemplo /var/lib/ultron/uploads en el servidor.");
        }

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento", ex);
        }
    }

    public Path getStorageLocation() {
        return fileStorageLocation;
    }

    /**
     * Guarda el archivo en {@code <upload-dir>/<folder>/<uuid>.<ext>} y devuelve la ruta relativa
     * ({@code folder/uuid.ext}), que es lo que se persiste en la base.
     */
    public String storeFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No se puede almacenar un archivo vacío");
        }

        if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido");
        }

        if (folder == null || !CARPETA_VALIDA.matcher(folder).matches()) {
            throw new IllegalArgumentException("La carpeta de destino no es válida");
        }

        String originalFilename = StringUtils.cleanPath(String.valueOf(file.getOriginalFilename()));
        String fileExtension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT)
                : "";

        if (!EXTENSIONES_PERMITIDAS.contains(fileExtension)) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPG, PNG, WEBP o GIF");
        }

        String fileName = UUID.randomUUID() + fileExtension;

        try {
            Path folderPath = this.fileStorageLocation.resolve(folder);
            Files.createDirectories(folderPath);

            Path targetLocation = folderPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return folder + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo almacenar el archivo " + fileName, ex);
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        Path path = resolverDentroDelAlmacenamiento(filePath);
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo eliminar el archivo: " + filePath, ex);
        }
    }

    private Path resolverDentroDelAlmacenamiento(String filePath) {
        Path path = this.fileStorageLocation.resolve(filePath).normalize();
        if (!path.startsWith(this.fileStorageLocation) || path.equals(this.fileStorageLocation)) {
            throw new IllegalArgumentException("La ruta del archivo no es válida");
        }
        return path;
    }
}
