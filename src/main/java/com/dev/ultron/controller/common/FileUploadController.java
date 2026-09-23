package com.dev.ultron.controller.common;

import com.dev.ultron.dto.common.FileUploadResponse;
import com.dev.ultron.service.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Path;

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
        
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(filePath)
                .toUriString();

        FileUploadResponse response = new FileUploadResponse(
                file.getOriginalFilename(),
                filePath,
                fileDownloadUri,
                file.getContentType(),
                file.getSize()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filePath) {
        try {
            Path path = fileStorageService.loadFile(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/octet-stream";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam String filePath) {
        fileStorageService.deleteFile(filePath);
        return ResponseEntity.noContent().build();
    }
}
