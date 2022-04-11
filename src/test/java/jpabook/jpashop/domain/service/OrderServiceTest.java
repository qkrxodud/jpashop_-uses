package jpabook.jpashop.domain.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.exception.NotEnoughStockException;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        //when
        Long orderId =  orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertThat(OrderStatus.ORDER).as("상품 주문시 상태는 ORDER").isEqualTo(getOrder.getOrderStatus());
        assertThat(1).as("주문한 상품 종류 수가 정화개향 한다.").isIn(getOrder.getOrderItems().size());
        assertThat(10000*orderCount).as("주문 가격은 가격 * 수량이다.").isIn(getOrder.getTotalPrice());
        assertThat(8).as("주문 수량만큼 재고가 줄어야 한다.").isIn(book.getStockQuantity());
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);
        int orderCount = 11;
        //when
        assertThrows(NotEnoughStockException.class, ()-> {
            orderService.order(member.getId(), book.getId(), orderCount);
        });

    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        //when
        Long id = orderService.order(member.getId(), book.getId(), orderCount);
        orderService.cancelOrder(id);

        //then
        Order getOrder = orderRepository.findOne(id);
        assertThat(OrderStatus.CANCEL).as("주문 취소시 상태는 CANCEL이다.").isEqualTo(getOrder.getOrderStatus());
        assertThat(10).as("주문이 취소된 상품은 그만큼 재고가 증가해야 한다..").isEqualTo(book.getStockQuantity());

    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "경기", "123-123"));
        em.persist(member);
        return member;
    }

}