package com.yusufemrebilgin.order.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String type; // E.g., 'OrderCreatedEvent'

    @Column(nullable = false, length = 2000)
    private String payload;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean processed;

    @Column(insertable = false)
    private Instant processedAt;

    @PrePersist
    public void prePersist() {
        processed = false;
        createdAt = Instant.now();
    }

    public void markAsProcessed(Instant processedAt) {
        processed = true;
        this.processedAt = processedAt;
    }

}
