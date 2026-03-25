package com.back.domain.order.controller;

import com.back.domain.order.controller.docs.OrderControllerDocs;
import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.dto.create.OrderCreateResponseDto;
import com.back.domain.order.dto.merge.OrderMergeResponseDto;
import com.back.domain.order.dto.query.OrderQueryResponseDto;
import com.back.domain.order.dto.update.OrderUpdateRequestDto;
import com.back.domain.order.dto.update.OrderUpdateResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.service.OrderMergeService;
import com.back.domain.order.service.OrderService;
import com.back.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {
    private final OrderService orderService;
    private final OrderMergeService orderMergeService;

    //다건 조회
    @Override
    public List<OrderQueryResponseDto> findAll() {
        return orderService.findAll().stream()
                .map(OrderQueryResponseDto::from)
                .toList();
    }

    //id로 조회한 order 보여주기 : 단건 조회
    @Override
    public OrderQueryResponseDto findById(int id) {
        CoffeeOrder order = orderService.findById(id);
        return OrderQueryResponseDto.from(order);
    }

    @Override
    public ResponseEntity<ApiResponse<OrderCreateResponseDto>> createNewOrder(
            @Valid OrderCreateRequestDto requestDto
    ) {
        try {
            OrderCreateResponseDto responseDto =
                    orderService.createNewOrder(requestDto.email(), requestDto.orderStatements());
            return ResponseEntity.ok(ApiResponse.ok(responseDto));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 내부 오류가 발생했습니다"));
        }
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<OrderMergeResponseDto>> createOrMergeOrder(
            @Valid @RequestBody OrderCreateRequestDto requestDto
    ) {
        try {
            OrderMergeResponseDto responseDto =
                    orderMergeService.createOrMergeOrderWithTotalAmount(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(responseDto));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 내부 오류가 발생했습니다"));
        }
    }

    // 주문 수정
    @Override
    public ResponseEntity<OrderUpdateResponseDto> updateOrder(
            int orderId,
            int orderStatementId,
            OrderUpdateRequestDto requestDto) {
        OrderUpdateResponseDto response = orderService.updateOrder(orderId, orderStatementId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> removeOrderStatement(
            int orderId,
            int orderStatementId
    ) {
        orderService.removeStatementById(orderId, orderStatementId);
        return ResponseEntity.noContent().build();
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
}