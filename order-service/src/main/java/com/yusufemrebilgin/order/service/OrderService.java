package com.yusufemrebilgin.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yusufemrebilgin.order.model.entity.Order;
import com.yusufemrebilgin.order.model.entity.Outbox;
import com.yusufemrebilgin.order.model.event.NotificationSentEvent;
import com.yusufemrebilgin.order.model.event.OrderCreatedEvent;
import com.yusufemrebilgin.order.model.request.CreateOrderRequest;
import com.yusufemrebilgin.order.repository.OrderRepository;
import com.yusufemrebilgin.order.repository.OutboxRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private static final String TOPIC_NAME = "order.events.created";
    private static final String SUCCESS_TOPIC_NAME = TOPIC_NAME + ".success";
    private static final String CONSUMER_GROUP_ID = "order-service-group";

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public Order createOrder(CreateOrderRequest createRequest) {

        Order newOrderEntity = Order.builder()
                .customerName(createRequest.customerName())
                .customerEmail(createRequest.customerEmail())
                .totalPrice(createRequest.totalPrice())
                .build();

        newOrderEntity = orderRepository.save(newOrderEntity);

        OrderCreatedEvent event = new OrderCreatedEvent(
                newOrderEntity.getId(),
                newOrderEntity.getCustomerName(),
                newOrderEntity.getCustomerEmail(),
                newOrderEntity.getTotalPrice()
        );

        Outbox outbox = Outbox.builder()
                .aggregateId(newOrderEntity.getId())
                .aggregateType(Order.class.getSimpleName())
                .type(OrderCreatedEvent.class.getSimpleName())
                .payload(convertEventToJsonString(event))
                .build();

        outboxRepository.save(outbox);

        return newOrderEntity;
    }

    @KafkaListener(topics = SUCCESS_TOPIC_NAME, groupId = CONSUMER_GROUP_ID)
    public void listener(@Payload JsonNode message) throws JsonProcessingException {

        NotificationSentEvent receivedEvent = objectMapper.treeToValue(message, NotificationSentEvent.class);
        Outbox outboxEntity = outboxRepository.findByAggregateId(receivedEvent.aggregateId())
                .orElseThrow(EntityNotFoundException::new);

        outboxEntity.markAsProcessed(receivedEvent.processedAt());
        outboxRepository.save(outboxEntity);
        logger.info("Successfully processed notification for order with aggregateId '{}'", receivedEvent.aggregateId());
    }

    private String convertEventToJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            logger.error("An error occurred while processing event: {}", ex.getMessage(), ex);
            // Returning empty String for simple flow
            return "";
        }
    }

}
