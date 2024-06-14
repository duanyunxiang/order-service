package com.dyx.orderservice.order.event;

public record OrderAcceptedMessage(
        Long orderId
) {
}
