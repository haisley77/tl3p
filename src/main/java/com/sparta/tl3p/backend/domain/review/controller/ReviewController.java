package com.sparta.tl3p.backend.domain.review.controller;

import com.sparta.tl3p.backend.common.dto.SuccessResponseDto;
import com.sparta.tl3p.backend.common.type.ResponseCode;
import com.sparta.tl3p.backend.domain.review.dto.ReviewCreationRequestDto;
import com.sparta.tl3p.backend.domain.review.dto.ReviewResponseDto;
import com.sparta.tl3p.backend.domain.review.dto.ReviewUpdateRequestDto;
import com.sparta.tl3p.backend.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<SuccessResponseDto> createReview(@RequestBody ReviewCreationRequestDto requestDto) {
        reviewService.createReview(requestDto.getOrderId(), requestDto.getContent(), requestDto.getScore());
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.S)
                        .message("리뷰가 등록되었습니다.")
                        .data(null)
                        .build()
        );
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<SuccessResponseDto> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody ReviewUpdateRequestDto requestDto) {
        reviewService.updateReview(reviewId, requestDto.getContent(), requestDto.getScore());
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.S)
                        .message("리뷰가 수정되었습니다.")
                        .data(null)
                        .build()
        );

    }

    @GetMapping
    public ResponseEntity<SuccessResponseDto> searchReviews(
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) String query) {
        List<ReviewResponseDto> responseDtos = reviewService.searchReviews(storeId, query);
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.NS)
                        .message("리뷰 검색에 성공했습니다.")
                        .data(responseDtos)
                        .build()
        );

    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<SuccessResponseDto> findReview(@PathVariable UUID reviewId) {
        ReviewResponseDto responseDto = reviewService.findReview(reviewId);
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.NS)
                        .message("리뷰 단건 조회 성공")
                        .data(responseDto)
                        .build()
        );
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<SuccessResponseDto> hideReview(@PathVariable UUID reviewId) {
        reviewService.hideReview(reviewId);
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.S)
                        .message("리뷰가 삭제되었습니다.")
                        .data(null)
                        .build()
        );
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<SuccessResponseDto> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.NS)
                        .message("리뷰가 영구적으로 삭제되었습니다.")
                        .data(null)
                        .build()
        );
    }
}
