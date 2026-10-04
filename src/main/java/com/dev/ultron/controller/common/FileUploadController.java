package com.dev.ultron.controller.common;

import com.dev.ultron.config.UploadsResourceConfig;
import com.dev.ultron.dto.common.FileUploadResponse;
import com.dev.ultron.service.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {

        String filePath = fileStorageService.storeFile(file, folder);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(UploadsResourceConfig.PUBLIC_PATH)
                .path(filePath)
                .toUriString();

        FileUploadResponse response = new FileUploadResponse(
                file.getOriginalFilename(),
                filePath,
                url,
                file.getContentType(),
                file.getSize()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam String filePath) {
        fileStorageService.deleteFile(filePath);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
