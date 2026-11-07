package com.agora.product;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProductFilterRequest(
        @Size(max = 255, message = "Keyword tìm kiếm vượt quá độ dài cho phép")
        String keyword,

        @PositiveOrZero(message = "Khoảng điểm của sách bắt đầu không hợp lệ")
        @Max(value = 5, message = "Khoảng điểm của sách bắt đầu không hợp lệ")
        Integer fromRating,

        @PositiveOrZero(message = "Khoảng điểm của sách kết thúc không hợp lệ")
        @Max(value = 5, message = "Khoảng điểm của sách kết thúc không hợp lệ")
        Integer toRating,

        UUID categoryId,
        UUID manufacturerId) {}
