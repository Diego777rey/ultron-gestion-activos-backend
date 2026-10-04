package com.dev.ultron.config;

import com.dev.ultron.service.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Publica la carpeta de archivos subidos del servidor en {@code /uploads/**}.
 * Los nombres son UUID y nunca se sobreescriben, así que el cliente puede cachearlos sin revalidar.
 */
@Configuration
@RequiredArgsConstructor
public class UploadsResourceConfig implements WebMvcConfigurer {

    public static final String PUBLIC_PATH = "/uploads/";

    private final FileStorageService fileStorageService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = fileStorageService.getStorageLocation().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler(PUBLIC_PATH + "**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
    }
}
