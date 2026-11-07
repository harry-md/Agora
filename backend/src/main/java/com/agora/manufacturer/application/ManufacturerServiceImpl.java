package com.agora.manufacturer.application;

import com.agora.address.CountryService;
import com.agora.exception.BadRequestException;
import com.agora.exception.ResourceNotFoundException;
import com.agora.manufacturer.*;
import com.agora.manufacturer.domain.Manufacturer;
import com.agora.manufacturer.persistence.ManufacturerRepository;
import com.agora.media.ImageStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ManufacturerMapper manufacturerMapper;
    private final ImageStorageService imageStorageService;
    private final CountryService countryService;
    private final TransactionTemplate transactionTemplate;

    private record UpdateResult(ManufacturerAdminResponse response, String oldLogoUrl) {}

    @Override
    @Cacheable(cacheNames = "manufacturer", key = "#slug")
    public ManufacturerResponse getBySlug(String slug) {
        return manufacturerRepository
                .findBySlug(slug)
                .map(manufacturerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà sản xuất"));
    }

    @Override
    @Cacheable(cacheNames = "admin-manufacturers")
    public Page<ManufacturerAdminResponse> getAllForAdmin(Pageable pageable) {
        return manufacturerRepository.findAllAdmin(pageable);
    }

    @Override
    @CacheEvict(cacheNames = "admin-manufacturers", allEntries = true)
    public ManufacturerResponse create(CreateManufacturerRequest request) {
        validateCountry(request.countryId());
        String baseSlug = toSlug(request.name());
        if (request.logo() == null || request.logo().isEmpty()) {
            throw new BadRequestException("Logo không được để trống");
        }
        String logoUrl = imageStorageService.upload(request.logo());
        try {
            return transactionTemplate.execute(status -> {
                Manufacturer manufacturer = manufacturerMapper.toEntity(request);
                manufacturer.setName(request.name().trim());
                manufacturer.setSlug(uniqueSlug(baseSlug));
                manufacturer.setLogoUrl(logoUrl);
                return manufacturerMapper.toResponse(manufacturerRepository.save(manufacturer));
            });
        } catch (RuntimeException ex) {
            cleanupUploadedImage(logoUrl, ex);
            throw ex;
        }
    }

    @Override
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "manufacturer", key = "#result.slug"),
                @CacheEvict(cacheNames = "admin-manufacturers", allEntries = true)
            })
    public ManufacturerAdminResponse update(UUID id, UpdateManufacturerRequest request) {
        if (!manufacturerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhà sản xuất");
        }

        String countryName = countryService.getNameById(request.countryId());
        String newLogoUrl = request.logo() != null && !request.logo().isEmpty()
                ? imageStorageService.upload(request.logo())
                : null;

        UpdateResult result;
        try {
            result = transactionTemplate.execute(status -> {
                Manufacturer manufacturer = manufacturerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Không tìm thấy nhà sản xuất"));
                String oldLogoUrl = manufacturer.getLogoUrl();
                manufacturerMapper.updateEntity(request, manufacturer);
                manufacturer.setName(request.name().trim());

                if (newLogoUrl != null) {
                    manufacturer.setLogoUrl(newLogoUrl);
                }
                return new UpdateResult(
                        manufacturerMapper.toAdminResponse(manufacturer, countryName), oldLogoUrl);
            });
        } catch (RuntimeException ex) {
            if (newLogoUrl != null) {
                cleanupUploadedImage(newLogoUrl, ex);
            }
            throw ex;
        }

        if (newLogoUrl != null && !newLogoUrl.equals(result.oldLogoUrl())) {
            try {
                imageStorageService.delete(result.oldLogoUrl());
            } catch (RuntimeException | IOException ex) {
                log.error("Error while cleaning up replaced manufacturer logo", ex);
            }
        }
        return result.response();
    }

    @Override
    public boolean existsById(UUID id) {
        return manufacturerRepository.existsById(id);
    }

    private void validateCountry(UUID countryId) {
        if (!countryService.existsById(countryId)) {
            throw new ResourceNotFoundException("Không tìm thấy quốc gia");
        }
    }

    private String toSlug(String name) {
        String slug = name.trim().toLowerCase(Locale.ROOT).replace("đ", "d");
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        slug = slug.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isEmpty()) {
            throw new BadRequestException("Tên nhà sản xuất không tạo được slug hợp lệ");
        }
        return slug.substring(0, Math.min(slug.length(), 150)).replaceAll("-+$", "");
    }

    private String uniqueSlug(String base) {
        String slug = base;

        // "admin" is reserved for GET /api/manufacturers/admin.
        while (slug.equals("admin") || manufacturerRepository.existsBySlug(slug)) {
            String suffix = String.format(Locale.ROOT, "%04d", (int) (Math.random() * 10001));
            int maxBaseLength = 150 - 1 - suffix.length();
            slug = base.substring(0, Math.min(base.length(), maxBaseLength)) + "-" + suffix;
        }
        return slug;
    }

    private void cleanupUploadedImage(String logoUrl, RuntimeException original) {
        try {
            imageStorageService.delete(logoUrl);
        } catch (RuntimeException | IOException cleanupFailure) {
            original.addSuppressed(cleanupFailure);
            log.error(
                    "Error while cleaning up manufacturer logo after failed write", cleanupFailure);
        }
    }
}
