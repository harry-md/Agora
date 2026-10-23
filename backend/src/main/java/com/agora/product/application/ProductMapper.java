package com.agora.product.application;

import com.agora.product.CreateProductRequest;
import com.agora.product.ProductResponse;
import com.agora.product.ProductSummaryResponse;
import com.agora.product.domain.Product;
import com.agora.product.domain.ProductDetail;
import com.agora.product.domain.ProductImage;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "thumbnail", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "description", source = "detail.description")
    @Mapping(target = "images", source = "detail.images")
    ProductResponse toResponse(
            Product product, ProductDetail detail, List<ProductResponse.SpecValueResponse> specs);

    ProductSummaryResponse toSummary(Product product);

    default String toImageUrl(ProductImage image) {
        return image.url();
    }
}
