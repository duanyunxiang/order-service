package com.dyx.orderservice.order.web;

import com.dyx.orderservice.order.domain.Order;
import com.dyx.orderservice.order.domain.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    //自动装配访问令牌对象：Jwt格式-代表当前认证用户，subject为用户名
    public Flux<Order> getAllOrders(@AuthenticationPrincipal Jwt jwt){
        return orderService.getAllOrders(jwt.getSubject());
    }

    @PostMapping
    public Mono<Order> submitOrder(@RequestBody @Valid OrderRequest orderRequest){
        return orderService.submitOrder(orderRequest.isbn(),orderRequest.quantity());
    }
}
