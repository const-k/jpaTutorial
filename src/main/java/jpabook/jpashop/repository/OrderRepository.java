package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

//    public List<Order> findAll(OrderSearch orderSearch) {
//        // Querydsl 로 처리
//        QOrder order = QOrder.order;
//        QMember member = Qmember.member;
//
//        return query
//                .select(order)
//                .from(order)
//                .join(order.member, member)
//                .where(statusEq(orderSearch.getOrderStatus()),
//                        nameLike(orderSearch.getMemberName()))
//                .limit(1000)
//                .fetch();
//
//    }
//
//    private BooleanExpressionPredicate statusEq(OrderStatus statusCond) {
//        if (statusCond == null) {
//            return null;
//        }
//
//        return order.status.eq(statusCond);
//    }
//
//    private BooleanExpressionPredicate nameLike(String nameCond) {
//        if (!StringUtils.hasText(nameCond)) {
//            return null;
//        }
//
//        return member.name.like(nameCond);
//    }


    /**
     * JPA Criteria
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" +
                    orderSearch.getMemberName() + "%");
            criteria.add(name);

        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() { // 한 번의 쿼리로 Order, Member, Delivery 다 조인 후 select 로 가져옴, LAZY 무시하고 다 값을 채워서 가져옴
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class).getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) { // 한 번의 쿼리로 Order, Member, Delivery 다 조인 후 select 로 가져옴, LAZY 무시하고 다 값을 채워서 가져옴
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    // NOTE 컬렌션(일대다) 페치 조인을 사용하면 페이징 처리를 DB에서 할 수 없음, 애플리케이션에 가져온 후 메모리에서 페이징 처리를 함
    // NOTE 페이징 처리 안되는 이유 -> 일대다 페치 조인 => jpa 결과 order = 2개, DB에서 쿼리로 조회한 order = 4개
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" +  // distinct = jpa 에서 자체적으로 Order Id가 같으면 중복을 제거(실제 DB에서는 중복 제거 안될 수 있음)
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" + // Order 2개, OrderItems = 4개 조인 -> Order 가 4개가 됨
                        " join fetch oi.item i", Order.class)
//                .setFirstResult(1)    // 페이징 처리
//                .setMaxResults(100)
                .getResultList();
    }
}
