package com.sparta.tl3p.backend.domain.ai.controller;

import com.sparta.tl3p.backend.common.dto.SuccessResponseDto;
import com.sparta.tl3p.backend.common.type.ResponseCode;
import com.sparta.tl3p.backend.domain.ai.dto.AIDescriptionRequestDto;
import com.sparta.tl3p.backend.domain.ai.service.AIDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AIDescriptionController {





    private final AIDescriptionService aiDescriptionService;

    @PostMapping("/items/ai-description")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SuccessResponseDto> createAIDescription(
            @RequestBody AIDescriptionRequestDto request
    ) {
        return ResponseEntity.ok(SuccessResponseDto.builder()
                .code(ResponseCode.S)
                .message("상품 설명 생성 요청 성공")
                .data(aiDescriptionService.generateDescription(request))
                .build());
    }
}
