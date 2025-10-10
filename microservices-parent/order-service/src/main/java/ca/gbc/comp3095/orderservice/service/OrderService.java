package ca.gbc.comp3095.orderservice.service;

import ca.gbc.comp3095.orderservice.dto.OrderRequest;

public interface OrderService {

    void placeOrder(OrderRequest orderRequest);

}
