package com.dyx.orderservice.order.web;

import com.dyx.orderservice.order.domain.Order;
import com.dyx.orderservice.order.domain.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public Flux<Order> getAllOrders(){
        return orderService.getAllOrders();
    }

    @PostMapping
    public Mono<Order> submitOrder(@RequestBody @Valid OrderRequest orderRequest){
        return orderService.submitOrder(orderRequest.isbn(),orderRequest.quantity());
    }
}
