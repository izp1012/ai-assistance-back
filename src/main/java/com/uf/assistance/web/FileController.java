package com.uf.assistance.web;

import com.uf.assistance.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource resource = fileStorageService.loadFileAsResource(filename);

        // 파일 확장자에 따라 MediaType 결정
        MediaType mediaType = getMediaTypeForFileName(filename);

        return ResponseEntity.ok()
                .contentType(mediaType) // 이미지 타입에 맞게 수정
                .body(resource);
    }

    // 파일명에 따라 적절한 MediaType 반환
    private MediaType getMediaTypeForFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
