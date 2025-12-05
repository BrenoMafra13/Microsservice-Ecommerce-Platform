package ca.gbc.comp3095.orderservice.service;

import ca.gbc.comp3095.orderservice.client.InventoryClient;
import ca.gbc.comp3095.orderservice.dto.OrderRequest;
import ca.gbc.comp3095.orderservice.event.OrderPlacedEvent;
import ca.gbc.comp3095.orderservice.model.Order;
import ca.gbc.comp3095.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    /*
    * KafkaTemplate is the client tool that lest us PRODUCE message events into our KafkaBroker
     */
    private final KafkaTemplate<String, OrderPlacedEvent>  kafkaTemplate;

    @Override
    public void placeOrder(OrderRequest orderRequest) {

        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

        if (isProductInStock) {
            Order order = Order.builder()
                    .orderNumber(UUID.randomUUID().toString())
                    .price(orderRequest.price())
                    .quantity(orderRequest.quantity())
                    .skuCode(orderRequest.skuCode())
                    .build();

            //persist the order to the order-service database
            orderRepository.save(order);

            OrderPlacedEvent orderPlacedEvent = new  OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
            orderPlacedEvent.setEmail(orderRequest.userDetails().email());
            orderPlacedEvent.setFirstName(orderRequest.userDetails().firstName());
            orderPlacedEvent.setLastName(orderRequest.userDetails().lastName());

            log.info("Start - sending OrderPlacedEvent {} to Kafka topic 'order-placed'", orderPlacedEvent);
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("Endt - OrderPlacedEvent {} sent to Kafka topic 'order-placed'", orderPlacedEvent);

        } else {

            throw new RuntimeException("Product with  skuCode: " + orderRequest.skuCode() + " is not in stock");

        }
    }
}
