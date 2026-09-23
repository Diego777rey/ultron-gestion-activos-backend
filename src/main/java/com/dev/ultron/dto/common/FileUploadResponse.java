package com.dev.ultron.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String fileName;
    private String filePath;
    private String fileDownloadUri;
    private String fileType;
    private long size;
}
