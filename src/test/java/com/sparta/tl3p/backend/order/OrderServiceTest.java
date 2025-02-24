package com.sparta.tl3p.backend.order;

import com.sparta.tl3p.backend.common.exception.BusinessException;
import com.sparta.tl3p.backend.common.type.Address;
import com.sparta.tl3p.backend.common.type.ErrorCode;
import com.sparta.tl3p.backend.domain.item.entity.Item;
import com.sparta.tl3p.backend.domain.item.repository.ItemRepository;
import com.sparta.tl3p.backend.domain.member.entity.Member;
import com.sparta.tl3p.backend.domain.member.enums.Role;
import com.sparta.tl3p.backend.domain.member.repository.MemberRepository;
import com.sparta.tl3p.backend.domain.order.dto.*;
import com.sparta.tl3p.backend.domain.order.entity.Order;
import com.sparta.tl3p.backend.domain.order.enums.OrderType;
import com.sparta.tl3p.backend.domain.order.enums.PaymentMethod;
import com.sparta.tl3p.backend.domain.order.repository.OrderRepository;
import com.sparta.tl3p.backend.domain.order.service.OrderService;
import com.sparta.tl3p.backend.domain.payment.dto.PaymentRequestDto;
import com.sparta.tl3p.backend.domain.payment.dto.PaymentResponseDto;
import com.sparta.tl3p.backend.domain.payment.entity.Payment;
import com.sparta.tl3p.backend.domain.payment.enums.PaymentStatus;
import com.sparta.tl3p.backend.domain.payment.service.PaymentService;
import com.sparta.tl3p.backend.domain.store.entity.Store;
import com.sparta.tl3p.backend.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock private PaymentService paymentService;
    @Mock private OrderRepository orderRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private ItemRepository itemRepository;

    // 공통 목 객체 (setUp에서는 생성만)
    private Member customer;
    private Member owner;
    private Member anotherMember;
    private Store store;
    private OrderRequestDto orderRequestDto;
    private OrderUpdateRequestDto orderUpdateRequestDto;
    private OrderCancelRequestDto orderCancelRequestDto;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        customer = mock(Member.class);
        owner = mock(Member.class);
        anotherMember = mock(Member.class);
        store = mock(Store.class);
        orderRequestDto = mock(OrderRequestDto.class);
        orderUpdateRequestDto = mock(OrderUpdateRequestDto.class);
        orderCancelRequestDto = mock(OrderCancelRequestDto.class);
        orderId = UUID.randomUUID();
    }

    /* ========== createOrder 테스트 ========== */
    @Test
    void createOrder_success() {
        // 고객 정보
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        // OrderRequestDto stubbing
        UUID storeId = UUID.randomUUID();
        when(orderRequestDto.getStoreId()).thenReturn(storeId);
        when(orderRequestDto.getOrderType()).thenReturn(OrderType.ONLINE);
        when(orderRequestDto.getPaymentMethod()).thenReturn(PaymentMethod.CARD);

        Address address = new Address("Seoul", "Main Street", "12345");
        when(orderRequestDto.getDeliveryAddress()).thenReturn(address);
        when(orderRequestDto.getStoreRequest()).thenReturn("당도 낮게 부탁드립니다.");

        // 주문 아이템 stubbing
        OrderItemRequestDto orderItem = mock(OrderItemRequestDto.class);
        UUID itemId = UUID.randomUUID();
        when(orderItem.getItemId()).thenReturn(itemId);
        when(orderItem.getQuantity()).thenReturn(2);
        when(orderRequestDto.getItems()).thenReturn(Collections.singletonList(orderItem));

        // 아이템 조회
        Item item = mock(Item.class);
        when(item.getPrice()).thenReturn(BigDecimal.valueOf(1000));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // 리포지토리 stubbing
        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // 결제 stubbing
        PaymentResponseDto paymentResponse = mock(PaymentResponseDto.class);
        when(paymentResponse.getPaymentStatus()).thenReturn(PaymentStatus.SUCCESS);
        Payment payment = mock(Payment.class);
        when(paymentResponse.toPayment()).thenReturn(payment);

        when(paymentService.requestPayment(any(Order.class), any(PaymentRequestDto.class)))
                .thenReturn(paymentResponse);

        // 주문 저장 stubbing
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setOrderId(orderId);
            return o;
        });

        // 실행
        OrderResponseDto response = orderService.createOrder(orderRequestDto, 1L);

        // 검증
        assertThat(response.getOrderId()).isEqualTo(orderId);
        verify(paymentService).requestPayment(any(Order.class), any(PaymentRequestDto.class));
    }

    @Test
    void createOrder_paymentFailure_throwsException() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        UUID storeId = UUID.randomUUID();
        when(orderRequestDto.getStoreId()).thenReturn(storeId);
        when(orderRequestDto.getOrderType()).thenReturn(OrderType.ONLINE);
        when(orderRequestDto.getPaymentMethod()).thenReturn(PaymentMethod.CARD);

        Address address = new Address("Seoul", "Main Street", "12345");
        when(orderRequestDto.getDeliveryAddress()).thenReturn(address);
        when(orderRequestDto.getStoreRequest()).thenReturn("당도 낮게 부탁드립니다.");

        OrderItemRequestDto orderItem = mock(OrderItemRequestDto.class);
        UUID itemId = UUID.randomUUID();
        when(orderItem.getItemId()).thenReturn(itemId);
        when(orderItem.getQuantity()).thenReturn(2);
        when(orderRequestDto.getItems()).thenReturn(Collections.singletonList(orderItem));

        Item item = mock(Item.class);
        when(item.getPrice()).thenReturn(BigDecimal.valueOf(1000));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // 결제 실패 시뮬레이션
        PaymentResponseDto paymentResponse = mock(PaymentResponseDto.class);
        when(paymentResponse.getPaymentStatus()).thenReturn(PaymentStatus.FAILED);

        when(paymentService.requestPayment(any(Order.class), any(PaymentRequestDto.class)))
                .thenReturn(paymentResponse);

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.createOrder(orderRequestDto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.PAYMENT_FAILED.getMessage());
    }

    @Test
    void createOrder_memberNotFound_throwsException() {
        // "memberRepository.findById(1L)"가 Empty를 반환하도록
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.createOrder(orderRequestDto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void createOrder_storeNotFound_throwsException() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        UUID storeId = UUID.randomUUID();
        when(orderRequestDto.getStoreId()).thenReturn(storeId);
        when(orderRequestDto.getOrderType()).thenReturn(OrderType.ONLINE);
        when(orderRequestDto.getPaymentMethod()).thenReturn(PaymentMethod.CARD);

        Address address = new Address("Seoul", "Main Street", "12345");
        when(orderRequestDto.getDeliveryAddress()).thenReturn(address);
        when(orderRequestDto.getStoreRequest()).thenReturn("당도 낮게 부탁드립니다.");

        OrderItemRequestDto orderItem = mock(OrderItemRequestDto.class);
        UUID itemId = UUID.randomUUID();
        when(orderItem.getItemId()).thenReturn(itemId);
        when(orderItem.getQuantity()).thenReturn(2);
        when(orderRequestDto.getItems()).thenReturn(Collections.singletonList(orderItem));

        Item item = mock(Item.class);
        when(item.getPrice()).thenReturn(BigDecimal.valueOf(1000));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));
        // storeRepository가 empty를 반환 → STORE_NOT_FOUND 예외 유발
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.createOrder(orderRequestDto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.STORE_NOT_FOUND.getMessage());
    }

    @Test
    void createOrder_itemNotFound_throwsException() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        UUID storeId = UUID.randomUUID();
        when(orderRequestDto.getStoreId()).thenReturn(storeId);
        when(orderRequestDto.getOrderType()).thenReturn(OrderType.ONLINE);
        when(orderRequestDto.getPaymentMethod()).thenReturn(PaymentMethod.CARD);

        Address address = new Address("Seoul", "Main Street", "12345");
        when(orderRequestDto.getDeliveryAddress()).thenReturn(address);
        when(orderRequestDto.getStoreRequest()).thenReturn("당도 낮게 부탁드립니다.");

        OrderItemRequestDto missingItem = mock(OrderItemRequestDto.class);
        UUID missingItemId = UUID.randomUUID();
        when(missingItem.getItemId()).thenReturn(missingItemId);
        when(missingItem.getQuantity()).thenReturn(3);

        when(orderRequestDto.getItems()).thenReturn(Collections.singletonList(missingItem));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        // item이 존재하지 않도록
        when(itemRepository.findById(missingItemId)).thenReturn(Optional.empty());

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.createOrder(orderRequestDto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ITEM_NOT_FOUND.getMessage());
    }

    /* ========== updateOrder 테스트 ========== */
    @Test
    void updateOrder_customer_successWithinTime() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        // 주문 생성 (spy로 감싼 뒤, 내부 getMember() 호출 시 customer 반환)
        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        // 실행
        OrderResponseDto response = orderService.updateOrder(orderId, orderUpdateRequestDto, 1L);

        // 검증
        verify(existingOrder).updateOrder(orderUpdateRequestDto);
        assertThat(response.getOrderId()).isEqualTo(existingOrder.getOrderId());
    }

    @Test
    void updateOrder_customer_timeOut_throwsException() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        // 주문 생성 후 6분 전으로 설정
        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now().minusMinutes(6));
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.updateOrder(orderId, orderUpdateRequestDto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ORDER_TIME_OUT.getMessage());
    }

    @Test
    void updateOrder_customer_accessDenied() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        // 주문 주인이 anotherMember라면
        when(anotherMember.getMemberId()).thenReturn(2L);

        Order existingOrder = spy(new Order(orderRequestDto, anotherMember, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(anotherMember).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));

        // 실행 & 검증 (로그인 유저=1L, 주문주인=2L → Access Denied)
        assertThatThrownBy(() -> orderService.updateOrder(orderId, orderUpdateRequestDto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    void updateOrder_owner_success() {
        // 가게 주인
        when(owner.getMemberId()).thenReturn(2L);
        when(owner.getRole()).thenReturn(Role.OWNER);
        when(store.getMember()).thenReturn(owner);

        // 주문은 customer가 했으나, 점주(owner)가 변경 가능
        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        // 실행
        OrderResponseDto response = orderService.updateOrder(orderId, orderUpdateRequestDto, 2L);

        // 검증
        verify(existingOrder).updateOrder(orderUpdateRequestDto);
        assertThat(response.getOrderId()).isEqualTo(existingOrder.getOrderId());
    }

    @Test
    void updateOrder_owner_accessDenied() {
        when(owner.getMemberId()).thenReturn(2L);
        when(owner.getRole()).thenReturn(Role.OWNER);

        // 가게 실제 소유주는 anotherMember
        when(anotherMember.getMemberId()).thenReturn(3L);
        when(store.getMember()).thenReturn(anotherMember);

        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(owner));

        // 실행 & 검증 (로그인=2L, but store 주인=3L)
        assertThatThrownBy(() -> orderService.updateOrder(orderId, orderUpdateRequestDto, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
    }

    /* ========== cancelOrder 테스트 ========== */
    @Test
    void cancelOrder_customer_successWithinTime() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        // 실행
        OrderResponseDto response = orderService.cancelOrder(orderId, orderCancelRequestDto, 1L);

        // 검증
        verify(existingOrder).cancelOrder();
        assertThat(response.getOrderId()).isEqualTo(existingOrder.getOrderId());
    }

    @Test
    void cancelOrder_customer_timeOut_throwsException() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now().minusMinutes(6));
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, orderCancelRequestDto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ORDER_TIME_OUT.getMessage());
    }

    @Test
    void cancelOrder_customer_accessDenied() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        when(anotherMember.getMemberId()).thenReturn(2L);

        Order existingOrder = spy(new Order(orderRequestDto, anotherMember, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(anotherMember).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, orderCancelRequestDto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    void cancelOrder_owner_success() {
        when(owner.getMemberId()).thenReturn(2L);
        when(owner.getRole()).thenReturn(Role.OWNER);
        when(store.getMember()).thenReturn(owner);

        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        // 실행
        OrderResponseDto response = orderService.cancelOrder(orderId, orderCancelRequestDto, 2L);

        // 검증
        verify(existingOrder).cancelOrder();
        assertThat(response.getOrderId()).isEqualTo(existingOrder.getOrderId());
    }

    @Test
    void cancelOrder_owner_accessDenied() {
        when(owner.getMemberId()).thenReturn(2L);
        when(owner.getRole()).thenReturn(Role.OWNER);

        // store 실제 소유주는 anotherMember
        when(anotherMember.getMemberId()).thenReturn(3L);
        when(store.getMember()).thenReturn(anotherMember);

        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(owner));

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, orderCancelRequestDto, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
    }

    /* ========== getOrderDetail 테스트 ========== */
    @Test
    void getOrderDetail_customer_success() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));

        // 실행
        OrderDetailResponseDto detail = orderService.getOrderDetail(orderId, 1L);

        // 검증
        assertThat(detail).isNotNull();
    }

    @Test
    void getOrderDetail_customer_accessDenied() {
        when(customer.getMemberId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(Role.CUSTOMER);

        // 주문 자체는 anotherMember의 것
        when(anotherMember.getMemberId()).thenReturn(3L);

        Order existingOrder = spy(new Order(orderRequestDto, anotherMember, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(anotherMember).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(customer));

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.getOrderDetail(orderId, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    void getOrderDetail_owner_success() {
        when(owner.getMemberId()).thenReturn(2L);
        when(owner.getRole()).thenReturn(Role.OWNER);
        when(store.getMember()).thenReturn(owner);

        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(owner));

        // 실행
        OrderDetailResponseDto detail = orderService.getOrderDetail(orderId, 2L);

        // 검증
        assertThat(detail).isNotNull();
    }

    @Test
    void getOrderDetail_owner_accessDenied() {
        when(owner.getMemberId()).thenReturn(2L);
        when(owner.getRole()).thenReturn(Role.OWNER);

        // 가게의 실제 주인은 anotherMember
        when(anotherMember.getMemberId()).thenReturn(3L);
        when(store.getMember()).thenReturn(anotherMember);

        Order existingOrder = spy(new Order(orderRequestDto, customer, store));
        existingOrder.setCreatedAt(LocalDateTime.now());
        doReturn(customer).when(existingOrder).getMember();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(owner));

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.getOrderDetail(orderId, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
    }

    /* ========== 조회 메서드 테스트 ========== */
    @Test
    void getUserOrders_success() {
        // 보통 User ID만으로 검색하므로, 특별히 Role까지 설정할 필요는 없음.
        // 필요하다면 아래와 같이 설정 가능:
        // when(customer.getMemberId()).thenReturn(1L);
        // when(customer.getRole()).thenReturn(Role.CUSTOMER);

        Order existingOrder = new Order(orderRequestDto, customer, store);
        when(orderRepository.findByMemberMemberId(1L))
                .thenReturn(Collections.singletonList(existingOrder));

        assertThat(orderService.getUserOrders(1L)).hasSize(1);
    }

    @Test
    void getStoreOrders_success() {
        when(owner.getMemberId()).thenReturn(2L);
        when(owner.getRole()).thenReturn(Role.OWNER);

        // store의 실제 소유주를 owner로 지정
        when(store.getMember()).thenReturn(owner);

        Order existingOrder = new Order(orderRequestDto, customer, store);

        when(storeRepository.findById(any(UUID.class))).thenReturn(Optional.of(store));
        when(orderRepository.findByStoreStoreId(any(UUID.class)))
                .thenReturn(Collections.singletonList(existingOrder));

        // 실행 & 검증
        assertThat(orderService.getStoreOrders(UUID.randomUUID(), 2L)).hasSize(1);
    }

    @Test
    void getStoreOrders_accessDenied() {
        // 로그인 유저 = 2L, but store의 실제 주인은 anotherMember(3L)라고 가정
        when(anotherMember.getMemberId()).thenReturn(3L);
        when(store.getMember()).thenReturn(anotherMember);

        when(storeRepository.findById(any(UUID.class))).thenReturn(Optional.of(store));

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.getStoreOrders(UUID.randomUUID(), 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    void searchOrders_success() {
        Order existingOrder = new Order(orderRequestDto, customer, store);

        // 특정 storeName, productName으로 검색했을 때
        when(orderRepository.searchOrders(1L, "TestStore", "TestProduct"))
                .thenReturn(Collections.singletonList(existingOrder));

        // 실행 & 검증
        assertThat(orderService.searchOrders(1L, "TestStore", "TestProduct")).hasSize(1);
    }
}
