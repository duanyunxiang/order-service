package com.dyx.orderservice.order.event;

public record OrderDispatchedMessage(
        Long orderId
) {}
