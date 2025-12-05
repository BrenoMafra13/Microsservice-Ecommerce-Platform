package ca.gbc.comp3095.notificationservice.service;

import ca.gbc.comp3095.orderservice.event.OrderPlacedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    @Value("${spring.mail.from:comp3095@georgebrown.ca}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    @KafkaListener(topics = "order-placed", groupId = "notificationService")
    public void handleOrderPlacedEvent(OrderPlacedEvent orderPlacedEvent) {

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(orderPlacedEvent.getEmail());
            helper.setSubject(String.format("Your Order (%s) was successfully placed", orderPlacedEvent.getOrderNumber()));
            helper.setText(String.format(
                    """
                    Good Day %s %s,
                    
                    Your Order with order number %s was successfully placed.
                    
                    Thank you for you business
                    COMP3095 Staff
                    """,
                    orderPlacedEvent.getFirstName(), orderPlacedEvent.getLastName(), orderPlacedEvent.getOrderNumber()));

            mailSender.send(message);
            log.info("Order notification email was sent to {}", orderPlacedEvent.getEmail());
        } catch (MessagingException ex) {
            log.error("Failed to send email to {}", orderPlacedEvent.getEmail());
            throw new RuntimeException("Failed to send email to " + orderPlacedEvent.getEmail(), ex);
        }
    }
}
