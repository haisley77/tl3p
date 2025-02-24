package com.sparta.tl3p.backend.domain.review.controller;

import com.sparta.tl3p.backend.common.dto.SuccessResponseDto;
import com.sparta.tl3p.backend.common.type.ResponseCode;
import com.sparta.tl3p.backend.domain.member.entity.CustomUserDetails;
import com.sparta.tl3p.backend.domain.review.dto.ReviewCreationRequestDto;
import com.sparta.tl3p.backend.domain.review.dto.ReviewResponseDto;
import com.sparta.tl3p.backend.domain.review.dto.ReviewUpdateRequestDto;
import com.sparta.tl3p.backend.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<SuccessResponseDto> createReview(
            @RequestBody ReviewCreationRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.createReview(requestDto.getOrderId(), requestDto.getContent(), requestDto.getScore(), userDetails.getMemberId());
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.S)
                        .message("리뷰가 등록되었습니다.")
                        .data(null)
                        .build()
        );
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<SuccessResponseDto> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody ReviewUpdateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.updateReview(reviewId, requestDto.getContent(), requestDto.getScore(), userDetails.getMemberId());
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.S)
                        .message("리뷰가 수정되었습니다.")
                        .data(null)
                        .build()
        );

    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER','OWNER','MANAGER')")
    public ResponseEntity<SuccessResponseDto> searchReviews(
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) String query,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReviewResponseDto> responseDtos = reviewService.searchReviews(storeId, query);
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.NS)
                        .message("리뷰 검색 성공")
                        .data(responseDtos)
                        .build()
        );

    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER','OWNER','MANAGER')")
    public ResponseEntity<SuccessResponseDto> findReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
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
    @PreAuthorize("hasAnyAuthority('CUSTOMER','MANAGER')")
    public ResponseEntity<SuccessResponseDto> hideReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.hideReview(reviewId, userDetails.getMemberId());
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.S)
                        .message("리뷰가 삭제되었습니다.")
                        .data(null)
                        .build()
        );
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER','MANAGER')")
    public ResponseEntity<SuccessResponseDto> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getMemberId());
        return ResponseEntity.ok(
                SuccessResponseDto.builder()
                        .code(ResponseCode.NS)
                        .message("리뷰가 영구적으로 삭제되었습니다.")
                        .data(null)
                        .build()
        );
    }
}
