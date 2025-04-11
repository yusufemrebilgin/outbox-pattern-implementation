package com.yusufemrebilgin.notification.event;

import java.math.BigDecimal;

public record OutboxOrderCreatedEvent(
        String orderId,
        String customerName,
        String customerEmail,
        BigDecimal totalPrice
) {}
