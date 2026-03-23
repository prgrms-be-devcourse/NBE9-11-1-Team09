package com.back.domain.order.controller;

import com.back.domain.order.dto.query.OrderQueryResponseDto;
import com.back.domain.order.dto.update.OrderUpdateRequestDto;
import com.back.domain.order.dto.update.OrderUpdateResponseDto;
import com.back.domain.order.entity.CoffeeOrder;
import com.back.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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