package com.dyx.orderservice.order.domain;

import com.dyx.orderservice.book.BookClient;
import com.dyx.orderservice.book.BookDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class OrderService {
    private final BookClient bookClient;
    private final OrderRepository orderRepository;

    public Flux<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    /**
     * 提交订单
     */
    public Mono<Order> submitOrder(String isbn,int quantity){
        return bookClient
                //检查图书是否存在
                .getBookByIsbn(isbn)
                //图书存在，接受订单
                .map(book -> buildAcceptedOrder(book,quantity))
                //图书不存在，拒绝订单
                .defaultIfEmpty(buildRejectedOrder(isbn,quantity))
                //使用flatMap提取Mono<Order>中的Order，并传递给Repository保存订单
                .flatMap(orderRepository::save);
    }

    public static Order buildRejectedOrder(String bookIsbn, int quantity){
        return Order.of(bookIsbn,null,null,quantity,OrderStatus.REJECTED);
    }

    public static Order buildAcceptedOrder(BookDto book,int quantity){
        return Order.of(book.isbn(), book.title()+"-"+book.author(),book.price(),quantity,OrderStatus.ACCEPTED);
    }
}
