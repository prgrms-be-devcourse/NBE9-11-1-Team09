package com.back.domain.order.controller;

import com.back.domain.order.dto.common.ApiResponse;
import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.dto.create.OrderCreateResponseDto;
import com.back.domain.order.dto.query.OrderQueryResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    //다건 조회
    @GetMapping
    public List<OrderQueryResponseDto> findAll(){
        return orderService.findAll().stream()
                .map(OrderQueryResponseDto::from)
                .toList();
    }

    //id로 조회한 order 보여주기 : 단건 조회
    @GetMapping("/{id}")
    public OrderQueryResponseDto findById(@PathVariable int id){
        CoffeeOrder order = orderService.findById(id);
        return OrderQueryResponseDto.from(order);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponseDto>> create(
            @Valid @RequestBody OrderCreateRequestDto requestDto) {
        try {
            OrderCreateResponseDto responseDto =
                    orderService.createOrder(requestDto.email(), requestDto.orderStatements());
            return ResponseEntity.ok(ApiResponse.ok(responseDto));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 내부 오류가 발생했습니다"));
        }
    }

    //@Valid 에러를 ApiResponse로 변환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {

        String message = e.getBindingResult()
                .getFieldErrors()
                .getFirst()
                .getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }
    @DeleteMapping("/{orderId}/statement/{orderStatementId}")
    public ResponseEntity<Void> removeOrderStatement(
            @PathVariable int orderId,
            @PathVariable int orderStatementId
    ) {
        orderService.removeStatementById(orderId, orderStatementId);
        return ResponseEntity.noContent().build();
    }

    // 주문 수정
    @PutMapping("/{orderId}/statement/{orderStatementId}")
    public ResponseEntity<OrderUpdateResponseDto> updateOrder(
            @PathVariable int orderId,
            @PathVariable int orderStatementId,
            @RequestBody OrderUpdateRequestDto requestDto) {
        OrderUpdateResponseDto response = orderService.updateOrder(orderId, orderStatementId, requestDto);
        return ResponseEntity.ok(response);
    }
}