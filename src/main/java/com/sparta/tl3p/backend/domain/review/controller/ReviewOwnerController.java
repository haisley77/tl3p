package com.sparta.tl3p.backend.domain.review.controller;

import com.sparta.tl3p.backend.common.dto.SuccessResponseDto;
import com.sparta.tl3p.backend.common.type.ResponseCode;
import com.sparta.tl3p.backend.domain.member.entity.CustomUserDetails;
import com.sparta.tl3p.backend.domain.review.dto.ReviewResponseDto;
import com.sparta.tl3p.backend.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner/reviews")
public class ReviewOwnerController {
    private final ReviewService reviewService;

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<SuccessResponseDto> searchOwnerReviews(
            @RequestParam(required = false) UUID storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReviewResponseDto> responseDtos = reviewService.searchOwnerReviews(storeId,userDetails.getMemberId());
        return ResponseEntity.ok(SuccessResponseDto
                .builder()
                .code(ResponseCode.NS)
                .message("내 리뷰 검색 성공")
                .data(responseDtos)
                .build());
    }


}
