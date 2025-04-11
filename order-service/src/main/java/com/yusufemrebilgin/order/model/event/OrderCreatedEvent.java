package com.yusufemrebilgin.order.model.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        String orderId,
        String customerName,
        String customerEmail,
        BigDecimal totalPrice
) {}
