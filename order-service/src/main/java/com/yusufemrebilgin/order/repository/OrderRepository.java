package com.yusufemrebilgin.order.repository;

import com.yusufemrebilgin.order.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {}
