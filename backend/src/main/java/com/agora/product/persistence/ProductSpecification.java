package com.agora.product.persistence;

import com.agora.product.ProductFilterRequest;
import com.agora.product.domain.Product;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

public class ProductSpecification {
    public static Specification<Product> buildFilter(ProductFilterRequest request) {
        return Specification.allOf(
                searchKeyword(request.keyword()),
                fromRating(request.fromRating()),
                toRating(request.toRating()),
                hasCategoryId(request.categoryId()),
                hasManufacturerId(request.manufacturerId()));
    }

    public static Specification<Product> searchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Specification.unrestricted();
        }
        String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
    }

    public static Specification<Product> hasManufacturerId(UUID manufacturerId) {
        if (manufacturerId == null) {
            return Specification.unrestricted();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("manufacturerId"), manufacturerId);
    }

    public static Specification<Product> hasCategoryId(UUID categoryId) {
        if (categoryId == null) {
            return Specification.unrestricted();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<Product> fromRating(Integer fromRating) {
        if (fromRating == null) {
            return Specification.unrestricted();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                root.get("averageRating"), BigDecimal.valueOf(fromRating));
    }

    public static Specification<Product> toRating(Integer toRating) {
        if (toRating == null) {
            return Specification.unrestricted();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(
                root.get("averageRating"), BigDecimal.valueOf(toRating));
    }

    public static Specification<Product> isActive(boolean active) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), active);
    }
}
