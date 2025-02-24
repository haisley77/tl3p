package com.sparta.tl3p.backend.domain.review.service;

import com.sparta.tl3p.backend.common.exception.BusinessException;
import com.sparta.tl3p.backend.common.type.ErrorCode;
import com.sparta.tl3p.backend.domain.member.entity.Member;
import com.sparta.tl3p.backend.domain.member.repository.MemberRepository;
import com.sparta.tl3p.backend.domain.order.entity.Order;
import com.sparta.tl3p.backend.domain.order.repository.OrderRepository;
import com.sparta.tl3p.backend.domain.review.dto.ReviewResponseDto;
import com.sparta.tl3p.backend.domain.review.entity.Review;
import com.sparta.tl3p.backend.domain.review.entity.ReviewStatus;
import com.sparta.tl3p.backend.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createReview(UUID orderId, String content, Double score, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (member != order.getMember()) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER);
        }

        Review review = Review.createReview(content, score, order);
        reviewRepository.save(review);
    }

    @Transactional
    public void updateReview(UUID reviewId, String content, Double score, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (member != review.getOrder().getMember()) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER);
        }

        review.updateReview(content, score);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> searchReviews(UUID storeId, String query) {
        List<Review> reviews = reviewRepository.searchReviews(storeId, query);
        return reviews.stream().map(ReviewResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto findReview(UUID reviewId) {

        Review review = reviewRepository.findByReviewIdAndStatusNot(reviewId, ReviewStatus.DELETED)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        return new ReviewResponseDto(review);
    }

    @Transactional
    public void hideReview(UUID reviewId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = reviewRepository.findByReviewIdAndStatusNot(reviewId, ReviewStatus.DELETED)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_ALREADY_DELETED));

        if (member != review.getOrder().getMember()) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER);
        }

        review.hideReview();
    }

    @Transactional
    public void deleteReview(UUID reviewId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = reviewRepository.findByReviewIdAndStatusNot(reviewId, ReviewStatus.DELETED)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_ALREADY_DELETED));

        if (member != review.getOrder().getMember()) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER);
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> searchOwnerReviews(UUID storeId, Long memberId) {
        List<Review> reviews = reviewRepository.searchOwnerReviews(storeId, memberId);
        return reviews.stream().map(ReviewResponseDto::new).collect(Collectors.toList());
    }
}
