package com.back.global.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class ApiResponse<T> {
    private final boolean success;  // 성공 여부
    private final String message;   // 메시지 (성공/오류 내용)
    private final T data;           // 실제 데이터 (성공 시 DTO, 실패 시 null)

    // 성공 시 호출하는 편의 메서드
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("성공")
                .data(data)
                .build();
    }

    // 실패 시 호출하는 편의 메서드
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}