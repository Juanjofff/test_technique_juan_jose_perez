package ec.juanperez.test.technique.kafka;

import ec.juanperez.test.technique.events.CustomerCreatedEvent;
import ec.juanperez.test.technique.events.CustomerDeletedEvent;
import ec.juanperez.test.technique.events.CustomerUpdatedEvent;
import ec.juanperez.test.technique.model.CustomerReference;
import ec.juanperez.test.technique.repository.CustomerReferenceRepository;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"customer-created", "customer-updated", "customer-deleted"})
@DirtiesContext
@ActiveProfiles("test")
@Transactional
class CustomerEventConsumerTest {

    @Autowired
    private CustomerEventConsumer eventConsumer;

    @Autowired
    private CustomerReferenceRepository customerReferenceRepository;

    @BeforeEach
    void setUp() {
        customerReferenceRepository.deleteAll();
    }

    @Test
    void testConsumeCustomerCreatedEvent() {
        // Given
        CustomerCreatedEvent event = new CustomerCreatedEvent(
                1L,
                "Test Customer",
                "1234567890",
                "ACTIVE"
        );

        // When
        eventConsumer.consumeCustomerCreated(event);

        // Then
        Optional<CustomerReference> customerRef = customerReferenceRepository.findById(1L);
        assertTrue(customerRef.isPresent(), "CustomerReference should be created");
        assertEquals("Test Customer", customerRef.get().getName());
        assertEquals("1234567890", customerRef.get().getIdentification());
        assertEquals(StatusType.ACTIVE, customerRef.get().getStatus());
    }

    @Test
    void testConsumeCustomerUpdatedEvent() {
        // Given - Create initial customer reference
        CustomerReference existingRef = new CustomerReference();
        existingRef.setId(1L);
        existingRef.setName("Original Name");
        existingRef.setIdentification("1234567890");
        existingRef.setStatus(StatusType.ACTIVE);
        customerReferenceRepository.save(existingRef);

        CustomerUpdatedEvent event = new CustomerUpdatedEvent(
                1L,
                "Updated Name",
                "9876543210",
                "ACTIVE"
        );

        // When
        eventConsumer.consumeCustomerUpdated(event);

        // Then
        Optional<CustomerReference> customerRef = customerReferenceRepository.findById(1L);
        assertTrue(customerRef.isPresent(), "CustomerReference should exist");
        assertEquals("Updated Name", customerRef.get().getName());
        assertEquals("9876543210", customerRef.get().getIdentification());
    }

    @Test
    void testConsumeCustomerUpdatedEvent_NotFound() {
        // Given - No existing customer reference
        CustomerUpdatedEvent event = new CustomerUpdatedEvent(
                999L,
                "Non-existent",
                "0000000000",
                "ACTIVE"
        );

        // When
        eventConsumer.consumeCustomerUpdated(event);

        // Then - Should not throw exception, just log warning
        Optional<CustomerReference> customerRef = customerReferenceRepository.findById(999L);
        assertFalse(customerRef.isPresent(), "CustomerReference should not be created for non-existent customer");
    }

    @Test
    void testConsumeCustomerDeletedEvent() {
        // Given - Create initial customer reference
        CustomerReference existingRef = new CustomerReference();
        existingRef.setId(1L);
        existingRef.setName("Test Customer");
        existingRef.setIdentification("1234567890");
        existingRef.setStatus(StatusType.ACTIVE);
        customerReferenceRepository.save(existingRef);

        CustomerDeletedEvent event = new CustomerDeletedEvent(1L);

        // When
        eventConsumer.consumeCustomerDeleted(event);

        // Then
        Optional<CustomerReference> customerRef = customerReferenceRepository.findById(1L);
        assertTrue(customerRef.isPresent(), "CustomerReference should still exist");
        assertEquals(StatusType.DELETED, customerRef.get().getStatus());
    }

    @Test
    void testConsumeCustomerDeletedEvent_NotFound() {
        // Given - No existing customer reference
        CustomerDeletedEvent event = new CustomerDeletedEvent(999L);

        // When
        eventConsumer.consumeCustomerDeleted(event);

        // Then - Should not throw exception, just log warning
        Optional<CustomerReference> customerRef = customerReferenceRepository.findById(999L);
        assertFalse(customerRef.isPresent(), "CustomerReference should not exist");
    }
}

