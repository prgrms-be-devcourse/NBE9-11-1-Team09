package com.back.global.exceptionhandler;

import com.back.domain.order.dto.common.ApiResponse;
import com.back.domain.order.exception.NotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public ApiResponse<Void> handleOrderNotFoundException(NotFoundException e) {
        return ApiResponse.error(e.getMessage());
    }
}
