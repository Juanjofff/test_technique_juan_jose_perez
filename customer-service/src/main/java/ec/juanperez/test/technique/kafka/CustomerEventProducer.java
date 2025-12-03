package ec.juanperez.test.technique.kafka;

import ec.juanperez.test.technique.events.CustomerCreatedEvent;
import ec.juanperez.test.technique.events.CustomerDeletedEvent;
import ec.juanperez.test.technique.events.CustomerUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CUSTOMER_CREATED_TOPIC = "customer-created";
    private static final String CUSTOMER_UPDATED_TOPIC = "customer-updated";
    private static final String CUSTOMER_DELETED_TOPIC = "customer-deleted";

    public void publishCustomerCreated(CustomerCreatedEvent event) {
        log.info("Publishing CustomerCreatedEvent for customer ID: {}", event.getCustomerId());
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(CUSTOMER_CREATED_TOPIC, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("CustomerCreatedEvent published successfully for customer ID: {}", event.getCustomerId());
            } else {
                log.error("Failed to publish CustomerCreatedEvent for customer ID: {}", event.getCustomerId(), ex);
            }
        });
    }

    public void publishCustomerUpdated(CustomerUpdatedEvent event) {
        log.info("Publishing CustomerUpdatedEvent for customer ID: {}", event.getCustomerId());
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(CUSTOMER_UPDATED_TOPIC, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("CustomerUpdatedEvent published successfully for customer ID: {}", event.getCustomerId());
            } else {
                log.error("Failed to publish CustomerUpdatedEvent for customer ID: {}", event.getCustomerId(), ex);
            }
        });
    }

    public void publishCustomerDeleted(CustomerDeletedEvent event) {
        log.info("Publishing CustomerDeletedEvent for customer ID: {}", event.getCustomerId());
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(CUSTOMER_DELETED_TOPIC, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("CustomerDeletedEvent published successfully for customer ID: {}", event.getCustomerId());
            } else {
                log.error("Failed to publish CustomerDeletedEvent for customer ID: {}", event.getCustomerId(), ex);
            }
        });
    }
}

