package com.dyx.orderservice.order.event;

import com.dyx.orderservice.order.domain.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class OrderFunctions {
    @Bean
    public Consumer<Flux<OrderDispatchedMessage>> dispatchOrder(OrderService orderService){
        //根据派发消息更新数据中订单状态
        return flux->orderService.consumeOrderDispatchedEvent(flux)
                //打印日志
                .doOnNext(order->log.info("订单id：{}，已派发",order.id()))
                //订阅反应式流以激活
                .subscribe();
    }
}
