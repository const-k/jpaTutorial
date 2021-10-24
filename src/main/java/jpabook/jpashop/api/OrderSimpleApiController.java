package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne, OneToOne) 성능 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> allByCriteria = orderRepository.findAllByCriteria(new OrderSearch());

        for (Order order : allByCriteria) {
            order.getMember().getName(); // order.getMember() 까지는 프록시 객체, 실제 Member 객체의 파라미터 가져올 때 Lazy 가 강제 초기화 됨
            order.getDelivery().getDeliveryStatus();    // Lazy 강제 초기화
        }

        return allByCriteria;   // ByteBuddyInterceptor 관련 에러남, Member 객체로 부터 Json 만들려고 했는데 프록시 객체가 들어있기 때문
        // 'jackson-datatype-hibernate5' 사용해서 해결
    }

    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2() {
        // Order 2개
        // N + 1 문제 -> 1 + N / 1 = 주문 조회 1건, N = Order 2개 있으므로 2개 조회 (내부적으로 회원 및 배송 쿼리 = 회원 N + 배송 N)
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());

        List<SimpleOrderDto> collect = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return new Result(collect);
    }

    // v4 와 차이점
    // v3 = 여러 API 에서 재사용이 가능하다 -> DB에서 원하는 엔티티 가져옴, 그 후 코드상에서 원하는대로 컨버팅해주면 됨
    // v4 = 해당 api에 맞게 Dto를 조회했기 때문에 재사용이 불가능
    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> collect = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @GetMapping("/api/v4/simple-orders")
    public Result ordersV4() {
        List<OrderSimpleQueryDto> orders = orderSimpleQueryRepository.findOrderDtos();

        return new Result(orders);
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();         // Lazy 초기화, 영속성 컨텍스트가 영속성 찾아보고 없으면 DB에 쿼리 날림
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // Lazy 초기화
        }
    }
}
