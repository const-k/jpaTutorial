package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class InitDB {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Transactional
    @Component
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        public void dbInit1() {
            Member member = createMember("userA", new Address("서울", "1", "123"));
            em.persist(member);

            Book book1 = createBook("JPA1 Book", 10000, 100);
            Book book2 = createBook("JPA2 Book", 20000, 100);

            em.persist(book1);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);

            em.persist(order);
        }

        public void dbInit2() {
            Member member = createMember("userB", new Address("부산", "2", "332"));
            em.persist(member);

            Book book1 = createBook("SPRING1 Book", 20000, 200);
            Book book2 = createBook("SPRING2 Book", 40000, 300);

            em.persist(book1);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 4);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);

            em.persist(order);
        }

        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private Book createBook(String name, int price, int stockQuantity) {
            Book book = new Book();
            book.setName(name);
            book.setPrice(price);
            book.setStockQuantity(stockQuantity);

            return book;
        }

        private Member createMember(String name, Address address) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(address);

            return member;
        }
    }
}
