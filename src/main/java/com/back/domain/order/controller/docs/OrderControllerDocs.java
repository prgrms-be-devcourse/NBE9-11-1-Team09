package com.back.domain.order.controller.docs;

import com.back.domain.order.dto.create.OrderCreateRequestDto;
import com.back.domain.order.dto.create.OrderCreateResponseDto;
import com.back.domain.order.dto.merge.OrderMergeResponseDto;
import com.back.domain.order.dto.query.OrderQueryResponseDto;
import com.back.domain.order.dto.update.OrderUpdateRequestDto;
import com.back.domain.order.dto.update.OrderUpdateResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Order", description = "주문 요청 처리 API")
@RequestMapping("/api/v1/order")
public interface OrderControllerDocs {

    // 🔥 다건 조회
    @Operation(summary = "주문 목록 조회", description = "전체 주문을 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = OrderQueryResponseDto.class))
            )
    )
    @GetMapping
    List<OrderQueryResponseDto> findAll();


    // 🔥 단건 조회
    @Operation(summary = "주문 단건 조회", description = "주문 ID를 전달받아 해당 사용자의 전체 주문 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = OrderQueryResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-03-24T12:00:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "1 Order not found",
                                              "path": "/api/v1/order/1"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    OrderQueryResponseDto findById(
            @Parameter(description = "주문 ID", example = "1")
            @PathVariable int id
    );


    // 🔥 주문서 삭제
    @Operation(summary = "주문서 삭제", description = "id를 전달 받아 특정 사용자의 주문서를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공"
                    //content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문 또는 주문서를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "존재하지않는 Order_Id입니다."
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{orderId}/statement/{orderStatementId}")
    ResponseEntity<Void> removeOrderStatement(
            @Parameter(description = "주문 ID", example = "1")
            @PathVariable int orderId,

            @Parameter(description = "주문서 ID", example = "1")
            @PathVariable int orderStatementId
    );


    // 🔥 주문 수정
    @Operation(summary = "주문 수정", description = "주문서의 내용을 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = OrderUpdateResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문 또는 주문서를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-03-24T12:00:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "1번 주문을 찾을 수 없습니다.",
                                              "path": "/api/v1/order/1/statement/1"
                                            }
                                            """
                            )
                    )
            )
    })
    @PutMapping("/{orderId}/statement/{orderStatementId}")
    ResponseEntity<OrderUpdateResponseDto> updateOrder(
            @Parameter(description = "주문 ID", example = "1")
            @PathVariable int orderId,

            @Parameter(description = "주문서 ID", example = "1")
            @PathVariable int orderStatementId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "주문 수정 요청 DTO",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderUpdateRequestDto.class)
                    )
            )
            @RequestBody OrderUpdateRequestDto requestDto
    );

    @Operation(summary = "주문 생성", description = "새로운 주문 요청을 전달받아 저장합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "성공",
                                              "data": {
                                                        "orderId": 1,
                                                        "email": "test@test.com",
                                                        "createdAt": "2026-03-24T12:00:00"
                                                        }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "잘못된 입력입니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "서버 내부 오류가 발생했습니다"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    ResponseEntity<com.back.global.response.ApiResponse<OrderCreateResponseDto>> createNewOrder(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "주문 생성 요청 DTO",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderCreateRequestDto.class)
                    )
            )
            @RequestBody OrderCreateRequestDto requestDto
    );

    // 신규 병합 API 명세서
    @Operation(summary = "주문 병합 (총 금액 반환)", description = "새로운 주문 요청을 전달받아 기존 주문서와 병합하고, 누적된 총 결제 금액을 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "주문 병합 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "성공",
                                              "data": {
                                                        "orderStatementId": 2,
                                                        "email": "test@test.com",
                                                        "createdAt": "2026-03-24T14:30:00",
                                                        "totalAmount": 7500
                                                        }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "잘못된 입력입니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "서버 내부 오류가 발생했습니다"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/merge")
    ResponseEntity<com.back.global.response.ApiResponse<OrderMergeResponseDto>> createOrMergeOrder(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "주문 병합 요청 DTO",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderCreateRequestDto.class)
                    )
            )
            @RequestBody OrderCreateRequestDto requestDto
    );
}