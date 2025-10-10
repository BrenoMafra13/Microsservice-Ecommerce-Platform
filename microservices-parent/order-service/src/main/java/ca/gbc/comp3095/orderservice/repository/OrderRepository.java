package ca.gbc.comp3095.orderservice.repository;

import ca.gbc.comp3095.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
