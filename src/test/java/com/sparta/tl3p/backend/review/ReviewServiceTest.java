package com.sparta.tl3p.backend.review;

import com.sparta.tl3p.backend.common.exception.BusinessException;
import com.sparta.tl3p.backend.common.type.Address;
import com.sparta.tl3p.backend.common.type.ErrorCode;
import com.sparta.tl3p.backend.domain.member.entity.Member;
import com.sparta.tl3p.backend.domain.member.repository.MemberRepository;
import com.sparta.tl3p.backend.domain.order.entity.Order;
import com.sparta.tl3p.backend.domain.order.repository.OrderRepository;
import com.sparta.tl3p.backend.domain.review.dto.ReviewResponseDto;
import com.sparta.tl3p.backend.domain.review.entity.Review;
import com.sparta.tl3p.backend.domain.review.entity.ReviewStatus;
import com.sparta.tl3p.backend.domain.review.repository.ReviewRepository;
import com.sparta.tl3p.backend.domain.review.service.ReviewService;
import com.sparta.tl3p.backend.domain.store.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    private UUID reviewId;
    private Long memberId;
    private UUID orderId;
    private UUID storeId;
    private Address address;
    private Member member;
    private Store store;
    private Order order;
    private Review review;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        memberId = 1L;
        orderId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        address = mock(Address.class);
        lenient().when(address.getCity()).thenReturn("Seoul");
        lenient().when(address.getStreet()).thenReturn("Gangnam-gu 123");
        lenient().when(address.getZipcode()).thenReturn("12345");

        member = mock(Member.class);
        lenient().when(member.getMemberId()).thenReturn(1L);
        lenient().when(member.getUsername()).thenReturn("testUser");
        lenient().when(member.getNickname()).thenReturn("test nickname");
        lenient().when(member.getEmail()).thenReturn("test@gmail.com");
        lenient().when(member.getAddress()).thenReturn(address);

        store = mock(Store.class);
        lenient().when(store.getName()).thenReturn("store1");
        lenient().when(store.getContent()).thenReturn("store description");
        lenient().when(store.getMember()).thenReturn(member);

        order = mock(Order.class);
        lenient().when(order.getOrderId()).thenReturn(orderId);
        lenient().when(order.getStore()).thenReturn(store);
        lenient().when(order.getMember()).thenReturn(member);

        review = Review.createReview("created review", 4.5, order);

    }

    @Test
    @DisplayName("리뷰 검색 테스트 by storeId")
    void searchReviewByStoreIdTest_success() {
        // given
        UUID orderId1 = UUID.randomUUID();
        Order order1 = mock(Order.class);
        lenient().when(order1.getOrderId()).thenReturn(orderId1);
        lenient().when(order1.getStore()).thenReturn(store);
        lenient().when(order1.getMember()).thenReturn(member);

        UUID orderId2 = UUID.randomUUID();
        Order order2 = mock(Order.class);
        lenient().when(order2.getOrderId()).thenReturn(orderId2);
        lenient().when(order2.getStore()).thenReturn(store);
        lenient().when(order2.getMember()).thenReturn(member);

        Review review1 = Review.createReview("created review1", 3.0, order1);
        Review review2 = Review.createReview("created review2", 4.0, order2);

        when(reviewRepository.searchReviews(storeId, null)).thenReturn(List.of(review1, review2));

        // when
        List<ReviewResponseDto> responseDtos = reviewService.searchReviews(storeId, null);

        // then
        assertThat(responseDtos.size()).isEqualTo(2);
        responseDtos.forEach(review -> assertThat(review.getStoreName()).isEqualTo(store.getName()));
        responseDtos.forEach(review -> assertThat(review.getNickname()).isEqualTo(member.getNickname()));

    }

    @Test
    @DisplayName("리뷰 검색 테스트 by query")
    void searchReviewByQueryTest_success() {
        // todo 테스트 코드 작성
    }

    @Test
    @DisplayName("리뷰 검색 테스트 by storeId and query")
    void searchReviewByStoreIdAndQueryTest_success() {
        // todo 테스트 코드 작성
    }

    @Test
    @DisplayName("리뷰 단건 조회 테스트")
    void searchReviewTest_success() {
        // todo 튜터님 질문 ..
//        // given
//        when(reviewRepository.findByIdAndNotInReviewStatus(reviewId, ReviewStatus.DELETED)).thenReturn(Optional.of(review));
//
//        // when
//        ReviewResponseDto responseDto = reviewService.findReview(reviewId);
//
//        // then
//        assertThat(responseDto.getReviewId()).isEqualTo(reviewId);
//        assertThat(responseDto.getScore()).isEqualTo(4.5);
//        assertThat(responseDto.getContent()).isEqualTo("created review");
    }

    @Test
    @DisplayName("본인 리뷰 조회 테스트")
    void searchOwnerReviews_success() {
        // given
        UUID orderId1 = UUID.randomUUID();
        Order order1 = mock(Order.class);
        lenient().when(order1.getOrderId()).thenReturn(orderId1);
        lenient().when(order1.getStore()).thenReturn(store);
        lenient().when(order1.getMember()).thenReturn(member);

        UUID orderId2 = UUID.randomUUID();
        Order order2 = mock(Order.class);
        lenient().when(order2.getOrderId()).thenReturn(orderId2);
        lenient().when(order2.getStore()).thenReturn(store);
        lenient().when(order2.getMember()).thenReturn(member);

        Review review1 = Review.createReview("created review1", 3.0, order1);
        Review review2 = Review.createReview("created review2", 4.0, order2);
        when(reviewRepository.searchOwnerReviews(storeId, memberId)).thenReturn(List.of(review1, review2));

        // when
        List<ReviewResponseDto> responseDtos = reviewService.searchOwnerReviews(storeId, memberId);

        // then
        assertThat(responseDtos.size()).isEqualTo(2);
        assertThat(responseDtos).allMatch(review -> review.getStoreName().equals(store.getName()));
        assertThat(responseDtos).allMatch(review -> review.getNickname().equals(member.getNickname()));

    }

    @Test
    @DisplayName("리뷰 수정 테스트")
    void updateReviewTest_success() {
        // given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.CREATED);
        assertThat(review.getContent()).isEqualTo("created review");
        assertThat(review.getScore()).isEqualTo(4.5);

        // when
        reviewService.updateReview(reviewId, "updated review", 4.3, memberId);
        Review updatedReview = reviewRepository.findById(reviewId).get();

        // then
        assertThat(updatedReview.getStatus()).isEqualTo(ReviewStatus.UPDATED);
        assertThat(updatedReview.getContent()).isEqualTo("updated review");
        assertThat(updatedReview.getScore()).isEqualTo(4.3);
    }

    @Test
    @DisplayName("리뷰 수정 예외 테스트")
    void updateReviewTest_reviewNotFound() {
        // given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.updateReview(reviewId, "updated review", 4.3, memberId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰 숨김 테스트")
    void hideReviewTest_success() {
        // given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        reviewService.hideReview(reviewId, memberId);

        // then
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.DELETED);
    }

    @Test
    @DisplayName("리뷰 숨김 예외 테스트")
    void hideReviewTest_reviewNotFound() {
        // given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.hideReview(reviewId,memberId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰 삭제 테스트")
    void deleteReviewTest_success() {
        // given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        reviewService.deleteReview(reviewId,memberId);

        // then
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("리뷰 삭제 예외 테스트")
    void deleteReview_reviewNotFound() {
        // given
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.deleteReview(reviewId,memberId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }
}
