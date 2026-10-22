package com.agora.product.application;

import com.agora.category.CategoryService;
import com.agora.exception.BadRequestException;
import com.agora.exception.ResourceNotFoundException;
import com.agora.manufacturer.ManufacturerService;
import com.agora.media.ImageStorageService;
import com.agora.product.CreateProductRequest;
import com.agora.product.ProductResponse;
import com.agora.product.ProductService;
import com.agora.product.domain.*;
import com.agora.product.persistence.ProductDetailRepository;
import com.agora.product.persistence.ProductRepository;
import com.agora.product.persistence.ProductSpecValueRepository;
import com.agora.product.persistence.SpecDefinitionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final SpecDefinitionRepository specDefinitionRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductSpecValueRepository productSpecValueRepository;
    private final ImageStorageService imageStorageService;
    private final CategoryService categoryService;
    private final ManufacturerService manufacturerService;
    private final ProductMapper productMapper;
    private final TransactionTemplate transactionTemplate;
    private final SpecDefinitionMapper specDefinitionMapper;

    private record ValidateSpec(SpecDefinition definition, Object value) {}

    @Override
    public ProductResponse create(CreateProductRequest request) {
        if (!manufacturerService.existsById(request.manufacturerId())) {
            throw new ResourceNotFoundException("Không tìm thấy nhà sản xuất");
        }
        if (!categoryService.existsById(request.categoryId())) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục");
        }

        if (request.thumbnail() == null || request.thumbnail().isEmpty()) {
            throw new BadRequestException("Thumbnail không được để trống");
        }

        String baseSlug = toSlug(request.name());
        List<ValidateSpec> specs = validateSpecs(request.specs());
        List<String> uploadedUrls = new ArrayList<>();
        try {

            List<MultipartFile> files = new ArrayList<>();
            files.add(request.thumbnail());
            files.addAll(request.images());

            List<String> urls = imageStorageService.uploadAll(files, uploadedUrls);
            String thumbnailUrl = urls.getFirst();
            List<String> imageUrls = urls.subList(1, urls.size());
            return transactionTemplate.execute(
                    status -> saveProduct(request, baseSlug, thumbnailUrl, imageUrls, specs));
        } catch (RuntimeException ex) {
            cleanedupUploadImages(uploadedUrls, ex);
            throw ex;
        }
    }

    /**
     * Validating incoming SpecValueRequest by checking duplicate,
     * get entities from DB and mapping to ValidateSpec records
     **/
    private List<ValidateSpec> validateSpecs(List<CreateProductRequest.SpecValueRequest> requests) {
        Set<UUID> ids = new HashSet<>();
        for (var spec : requests) {
            if (!ids.add(spec.specDefinitionId())) {
                throw new BadRequestException("Thông số bị lặp");
            }
        }
        Map<UUID, SpecDefinition> definitions = specDefinitionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(SpecDefinition::getId, Function.identity()));
        return requests.stream()
                .map(spec -> {
                    SpecDefinition definition = definitions.get(spec.specDefinitionId());
                    if (definition == null) {
                        throw new ResourceNotFoundException("Không tìm thấy định nghĩa thông số");
                    }

                    return new ValidateSpec(definition, parseValue(definition, spec.value()));
                })
                .toList();
    }

    private Object parseValue(SpecDefinition definition, String raw) {
        return switch (definition.getDataType()) {
            case TEXT -> raw;
            case NUMBER -> {
                try {
                    yield new BigDecimal(raw);

                } catch (NumberFormatException ex) {
                    throw new BadRequestException(
                            "Thông số" + definition.getName() + " phải là số");
                }
            }
            case BOOLEAN -> {
                if (raw.equalsIgnoreCase("true")) {
                    yield Boolean.TRUE;
                } else if (raw.equalsIgnoreCase("false")) {
                    yield Boolean.FALSE;
                }
                throw new BadRequestException(
                        "Thông số '" + definition.getName() + "' phải là true hoặc false");
            }
        };
    }

    /**
     * Build Product obj from CreateProductRequest
     * Build ProductDetail, ProductSpecValue then save all
     */
    private ProductResponse saveProduct(
            CreateProductRequest request,
            String baseSlug,
            String thumbnailUrl,
            List<String> imageUrls,
            List<ValidateSpec> specs) {
        Product product = productMapper.toEntity(request);
        product.setSlug(uniqueSlug(baseSlug));
        product.setThumbnail(thumbnailUrl);
        Product saved = productRepository.save(product);

        ProductDetail detail = ProductDetail.builder()
                .product(saved)
                .description(request.description())
                .images(imageUrls.stream()
                        .map(url -> new ProductImage(url, saved.getName()))
                        .toList())
                .build();
        productDetailRepository.save(detail);
        productSpecValueRepository.saveAll(
                specs.stream().map(spec -> toSpecValue(saved, spec)).toList());
        List<ProductResponse.SpecValueResponse> specResponses = specs.stream()
                .map(spec -> new ProductResponse.SpecValueResponse(
                        specDefinitionMapper.toResponse(spec.definition()), spec.value()))
                .toList();
        return productMapper.toResponse(saved, detail, specResponses);
    }

    /**
     * Use to create ProductSpecValue object
     */
    private ProductSpecValue toSpecValue(Product product, ValidateSpec spec) {
        var builder = ProductSpecValue.builder().product(product).specDefinition(spec.definition());
        // The DB CHECK requires exactly one of the three value columns to be non-null
        switch (spec.definition().getDataType()) {
            case TEXT -> builder.textValue((String) spec.value());
            case NUMBER -> builder.numberValue((BigDecimal) spec.value());
            case BOOLEAN -> builder.booleanValue((Boolean) spec.value());
        }
        return builder.build();
    }

    private String toSlug(String name) {
        String slug = name.trim().toLowerCase(Locale.ROOT).replace("đ", "d");
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        slug = slug.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isEmpty()) {
            throw new BadRequestException("Tên sản phẩm không tạo được slug hợp lệ");
        }
        return slug.substring(0, Math.min(slug.length(), 150)).replaceAll("-+$", "");
    }

    private String uniqueSlug(String base) {
        String slug = base;
        while (productRepository.existsBySlug(slug)) {
            String suffix = String.format(Locale.ROOT, "%04d", (int) (Math.random() * 10001));
            int maxBaseLength = 150 - 1 - suffix.length();
            slug = base.substring(0, Math.min(base.length(), maxBaseLength)) + "-" + suffix;
        }
        return slug;
    }

    private void cleanedupUploadImages(List<String> urls, RuntimeException original) {
        for (String url : urls) {
            try {
                imageStorageService.delete(url);
            } catch (RuntimeException | IOException ex) {
                original.addSuppressed(ex);
                log.error("Error while cleaning up product image after failed write", ex);
            }
        }
    }
}
