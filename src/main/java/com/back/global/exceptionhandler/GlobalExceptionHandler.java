package com.back.global.exceptionhandler;

import com.back.domain.order.dto.common.ApiResponse;
import com.back.domain.order.exception.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFoundException(NotFoundException e) {
        return ResponseEntity.status(404)
                .body(ApiResponse.error(e.getMessage()));
    }
}
