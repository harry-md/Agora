package com.agora.media.internal;

import com.agora.exception.BadRequestException;
import com.agora.media.ImageStorageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
class CloudinaryStorageServiceImpl implements ImageStorageService {
    private final Cloudinary cloudinary;
    private final Tika tika;
    private static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> defaultUrls = Set.of(
            "https://res.cloudinary.com/dyjdromdd/image/upload/v1791283629/1760239073710_554416787599948448_g1065711509247428827_730f381d0001f3c22e7483cc5b21fef9_zwreqq.jpg");

    private void validate(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Ảnh vượt quá kích thước cho phép");
        }
        try {
            String mimeType = tika.detect(file.getInputStream());
            if (!mimeType.startsWith("image/")) {
                throw new BadRequestException("Định dạng file không hợp lệ");
            }
        } catch (IOException ex) {
            log.error("Error when read file {}", file.getName(), ex);
            throw new BadRequestException("Lỗi khi đọc file");
        }
    }

    private void validate(byte[] data) {
        if (data.length > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Ảnh vượt quá kích thước cho phép");
        }
        String mimeType = tika.detect(data);
        if (!mimeType.startsWith("image/")) {
            throw new BadRequestException("Định dạng file không hợp lệ");
        }
    }

    @Override
    public String upload(MultipartFile file) {
        validate(file);
        try {
            Map res = cloudinary
                    .uploader()
                    .upload(file.getBytes(), ObjectUtils.asMap("resource_type", "image"));
            return res.get("secure_url").toString();
        } catch (IOException ex) {
            log.error("Error when upload image to Cloudinary {}", file.getName(), ex);
            throw new BadRequestException("Lỗi khi upload ảnh");
        }
    }

    @Override
    public void delete(String url) {
        if (url == null || defaultUrls.contains(url)) {
            return;
        }
        String[] parts = url.split("/upload/");
        String path = parts[1];
        if (path.startsWith("v") && path.contains("/")) {
            path = path.substring(path.indexOf("/") + 1);
        }
        String publicId = path.substring(0, path.lastIndexOf("."));

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException ex) {
            log.error("Error when delete image {}", url, ex);
        }
    }
}
