package com.agora.category.persistence;

import com.agora.category.domain.Category;

import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {

    public static Specification<Category> isActive(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }
}
