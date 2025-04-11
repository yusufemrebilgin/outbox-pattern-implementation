package com.yusufemrebilgin.notification.event;

import java.time.Instant;

public record NotificationSentEvent(
        String aggregateId,
        String type,
        String status,
        Instant processedAt
) {}
