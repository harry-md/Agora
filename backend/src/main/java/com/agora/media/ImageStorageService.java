package com.agora.media;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageStorageService {
    String upload(MultipartFile file);

    /**
     * Deletes an image, propagating failures so the caller can decide how to recover.
     */
    void delete(String url) throws IOException;

    List<String> uploadAll(List<MultipartFile> files, List<String> uploadedUrls);
}
