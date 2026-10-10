package com.agora.config;

import com.agora.config.properties.CloudinaryProperties;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StorageConfig {
    private final CloudinaryProperties cloudinaryProps;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name",
                cloudinaryProps.cloudName(),
                "api_key",
                cloudinaryProps.apiKey(),
                "api_secret",
                cloudinaryProps.apiSecret(),
                "secure",
                true));
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }
}
