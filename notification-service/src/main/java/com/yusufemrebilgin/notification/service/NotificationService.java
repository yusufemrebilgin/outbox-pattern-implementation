package com.yusufemrebilgin.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yusufemrebilgin.notification.event.NotificationSentEvent;
import com.yusufemrebilgin.notification.event.OutboxOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private static final String TOPIC_NAME = "order.events.created";
    private static final String RETRY_TOPIC_NAME = TOPIC_NAME + ".retry";
    private static final String ERROR_TOPIC_NAME = TOPIC_NAME + ".error";
    private static final String SUCCESS_TOPIC_NAME = TOPIC_NAME + ".success";
    private static final String CONSUMER_GROUP_ID = "notification-service-group";

    private static final String RETRY_COUNT_KEY = "retryCount";
    private static final int RETRY_COUNT_LIMIT = 3;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = {TOPIC_NAME, RETRY_TOPIC_NAME}, groupId = CONSUMER_GROUP_ID)
    public void listener(@Payload JsonNode message) {
        int retryCount = getRetryCount(message);
        if (!isRetryAllowed(retryCount)) {
            logger.info("Retry limit exceed for the message '{}'", message);
            kafkaTemplate.send(ERROR_TOPIC_NAME, message);
            return;
        }

        try {
            JsonNode payload = objectMapper.readTree(message.get("payload").asText());
            ((ObjectNode) message).put(RETRY_COUNT_KEY, retryCount + 1);
            simulateNotification(payload);

            // No exceptions occurred and retry limit not reached
            // Sending to the success topic

            NotificationSentEvent event = new NotificationSentEvent(
                    payload.get("orderId").asText(),
                    "OrderCreatedEvent",
                    "success",
                    Instant.now()
            );

            kafkaTemplate.send(SUCCESS_TOPIC_NAME, event);

        } catch (Exception ex) {
            logger.error("Exception occurred while processing message: {}", message, ex);
            kafkaTemplate.send(RETRY_TOPIC_NAME, message);
        }
    }

    private void simulateNotification(JsonNode payload) throws Exception {
        if (Math.random() < 0.5) {
            throw new Exception("Simulated failure");
        }

        OutboxOrderCreatedEvent event = objectMapper.treeToValue(payload, OutboxOrderCreatedEvent.class);
        logger.info("Notification successfully sent for order '{}'", event.orderId());
    }

    private int getRetryCount(JsonNode message) {
        JsonNode retryCount = message.get(RETRY_COUNT_KEY);
        return retryCount == null ? 0 : retryCount.asInt();
    }

    private boolean isRetryAllowed(int retryCount) {
        return retryCount < RETRY_COUNT_LIMIT;
    }

}
