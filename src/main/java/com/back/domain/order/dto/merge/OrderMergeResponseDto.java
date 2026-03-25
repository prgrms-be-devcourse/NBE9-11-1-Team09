package com.back.domain.order.dto.merge; // 💡 패키지명 맞춰드렸습니다!

import com.back.domain.order.entity.OrderStatement;
import java.time.LocalDateTime;

public record OrderMergeResponseDto(
        Integer orderStatementId,
        String email,
        LocalDateTime createdAt,
        int totalAmount
) {
    public static OrderMergeResponseDto from(OrderStatement statement, String email) {
        int mergedTotalAmount = statement.getOrder().getStatements().stream()
                .mapToInt(OrderStatement::getTotalAmount)
                .sum();

        return new OrderMergeResponseDto(
                statement.getId(),
                email,
                statement.getCreateDate(),
                mergedTotalAmount
        );
    }
}