package com.dyx.orderservice.order.domain;

import com.dyx.orderservice.book.BookClient;
import com.dyx.orderservice.book.BookDto;
import com.dyx.orderservice.order.event.OrderAcceptedMessage;
import com.dyx.orderservice.order.event.OrderDispatchedMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class OrderService {
    private final BookClient bookClient;
    private final OrderRepository orderRepository;
    //以命令方式，发送数据到特定目的地
    private final StreamBridge streamBridge;

    public Flux<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    /**
     * 提交订单
     */
    //事务声明
    @Transactional
    public Mono<Order> submitOrder(String isbn,int quantity){
        return bookClient
                //检查图书是否存在
                .getBookByIsbn(isbn)
                //图书存在，接受订单
                .map(book -> buildAcceptedOrder(book,quantity))
                //图书不存在，拒绝订单
                .defaultIfEmpty(buildRejectedOrder(isbn,quantity))
                //使用flatMap提取Mono<Order>中的Order，并传递给Repository保存订单
                .flatMap(orderRepository::save)
                // 保存成功后，发布事件
                .doOnNext(this::publishOrderAcceptedEvent);
    }

    /**
     * 订单派发后更新数据中Order状态
     */
    public Flux<Order> consumeOrderDispatchedEvent(Flux<OrderDispatchedMessage> flux){
        //根据id读取记录
        return flux.flatMap(message->orderRepository.findById(message.orderId()))
                .map(this::buildDispatchedOrder)
                //更新订单状态为DISPATCHED
                .flatMap(orderRepository::save);
    }

    private Order buildDispatchedOrder(Order existingOrder){
        //只有状态改变，其它属性不变
        return new Order(
                existingOrder.id(),existingOrder.bookIsbn(),existingOrder.bookName(),
                existingOrder.bookPrice(), existingOrder.quantity(),
                OrderStatus.DISPATCHED,existingOrder.createDate(),
                existingOrder.lastModifiedDate(),existingOrder.version()
        );
    }

    private void publishOrderAcceptedEvent(Order order){
        if(!order.status().equals(OrderStatus.ACCEPTED)){
            //订单没有被接受，不作处理
            return;
        }
        var orderAcceptedMessage=new OrderAcceptedMessage(order.id());
        log.info("sending order accepted event with id: {}",order.id());

        //将消息显示发送至acceptedOrder-out-0绑定
        //由streamBridge自动创建acceptOrder-out-0绑定
        var result=streamBridge.send("acceptOrder-out-0",orderAcceptedMessage);
        log.info("result of sending data for order with id {}: {}",order.id(),result);
    }

    public static Order buildRejectedOrder(String bookIsbn, int quantity){
        return Order.of(bookIsbn,null,null,quantity,OrderStatus.REJECTED);
    }

    public static Order buildAcceptedOrder(BookDto book,int quantity){
        return Order.of(book.isbn(), book.title()+" - "+book.author(),book.price(),quantity,OrderStatus.ACCEPTED);
    }
}
