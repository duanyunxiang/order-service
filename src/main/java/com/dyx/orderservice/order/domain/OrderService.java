package com.dyx.orderservice.order.domain;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public Flux<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    /**
     * 提交订单
     */
    public Mono<Order> submitOrder(String isbn,int quantity){
        //使用Mono.just开启反应式流，类似Steam.of开启java Stream
        return Mono.just(buildRejectedOrder(isbn,quantity))
                //使用flatMap提取Mono<Order>中的Order，并传递给Repository使用
                .flatMap(orderRepository::save);
    }
    
    public static Order buildRejectedOrder(String bookIsbn, int quantity){
        return Order.of(bookIsbn,null,null,quantity,OrderStatus.REJECTED);
    }
}
