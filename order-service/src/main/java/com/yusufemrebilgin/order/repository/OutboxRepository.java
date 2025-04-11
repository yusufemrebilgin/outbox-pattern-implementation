package com.yusufemrebilgin.order.repository;

import com.yusufemrebilgin.order.model.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OutboxRepository extends JpaRepository<Outbox, String> {

    Optional<Outbox> findByAggregateId(String aggregateId);

}
