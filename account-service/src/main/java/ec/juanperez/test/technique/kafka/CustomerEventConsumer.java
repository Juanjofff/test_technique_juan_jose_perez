package ec.juanperez.test.technique.kafka;

import ec.juanperez.test.technique.events.CustomerCreatedEvent;
import ec.juanperez.test.technique.events.CustomerDeletedEvent;
import ec.juanperez.test.technique.events.CustomerUpdatedEvent;
import ec.juanperez.test.technique.model.CustomerReference;
import ec.juanperez.test.technique.repository.CustomerReferenceRepository;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerEventConsumer {

    private final CustomerReferenceRepository customerReferenceRepository;

    @KafkaListener(topics = "customer-created", groupId = "account-service-group", containerFactory = "customerCreatedKafkaListenerContainerFactory")
    @Transactional
    public void consumeCustomerCreated(CustomerCreatedEvent event) {
        log.info("Received CustomerCreatedEvent for customer ID: {}", event.getCustomerId());
        
        CustomerReference customerRef = new CustomerReference();
        customerRef.setId(event.getCustomerId());
        customerRef.setName(event.getName());
        customerRef.setIdentification(event.getIdentification());
        customerRef.setStatus(StatusType.valueOf(event.getStatus()));
        
        customerReferenceRepository.save(customerRef);
        log.info("CustomerReference created for customer ID: {}", event.getCustomerId());
    }

    @KafkaListener(topics = "customer-updated", groupId = "account-service-group", containerFactory = "customerUpdatedKafkaListenerContainerFactory")
    @Transactional
    public void consumeCustomerUpdated(CustomerUpdatedEvent event) {
        log.info("Received CustomerUpdatedEvent for customer ID: {}", event.getCustomerId());
        
        customerReferenceRepository.findById(event.getCustomerId())
                .ifPresentOrElse(
                        customerRef -> {
                            customerRef.setName(event.getName());
                            customerRef.setIdentification(event.getIdentification());
                            customerRef.setStatus(StatusType.valueOf(event.getStatus()));
                            customerReferenceRepository.save(customerRef);
                            log.info("CustomerReference updated for customer ID: {}", event.getCustomerId());
                        },
                        () -> log.warn("CustomerReference not found for customer ID: {}", event.getCustomerId())
                );
    }

    @KafkaListener(topics = "customer-deleted", groupId = "account-service-group", containerFactory = "customerDeletedKafkaListenerContainerFactory")
    @Transactional
    public void consumeCustomerDeleted(CustomerDeletedEvent event) {
        log.info("Received CustomerDeletedEvent for customer ID: {}", event.getCustomerId());
        
        customerReferenceRepository.findById(event.getCustomerId())
                .ifPresentOrElse(
                        customerRef -> {
                            customerRef.setStatus(StatusType.DELETED);
                            customerReferenceRepository.save(customerRef);
                            log.info("CustomerReference marked as deleted for customer ID: {}", event.getCustomerId());
                        },
                        () -> log.warn("CustomerReference not found for customer ID: {}", event.getCustomerId())
                );
    }
}

