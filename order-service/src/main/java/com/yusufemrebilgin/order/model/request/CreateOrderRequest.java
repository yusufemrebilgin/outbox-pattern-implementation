package com.yusufemrebilgin.order.model.request;

import java.math.BigDecimal;

public record CreateOrderRequest(
        String customerName,
        String customerEmail,
        BigDecimal totalPrice
) {}
