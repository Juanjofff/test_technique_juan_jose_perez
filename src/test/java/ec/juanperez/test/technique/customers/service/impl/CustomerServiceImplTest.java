package ec.juanperez.test.technique.customers.service.impl;

import ec.juanperez.test.technique.app.customers.service.impl.CustomerServiceImpl;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import ec.juanperez.test.technique.app.customers.dto.CustomerDTO;
import ec.juanperez.test.technique.app.customers.exception.CustomerDeletedException;
import ec.juanperez.test.technique.app.customers.exception.CustomerNotFoundException;
import ec.juanperez.test.technique.app.customers.mapper.CustomerMapper;
import ec.juanperez.test.technique.app.customers.model.Customer;
import ec.juanperez.test.technique.app.customers.repository.CustomerRepository;
import ec.juanperez.test.technique.app.person.enums.GenderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Juan Pérez");
        customer.setGender(GenderType.MALE);
        customer.setIdentification("1804474466");
        customer.setAddress("Sangolquí");
        customer.setPhone("+593984877686");
        customer.setPassword("password123");
        customer.setStatus(StatusType.ACTIVE);

        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("Juan Pérez");
        customerDTO.setGender(GenderType.MALE);
        customerDTO.setIdentification("1804474466");
        customerDTO.setAddress("Sangolquí");
        customerDTO.setPhone("+593984877686");
        customerDTO.setPassword("password123");
        customerDTO.setStatus(StatusType.ACTIVE);
    }

    @Test
    void testCreateCustomer_Success() {
        // Given
        when(mapper.toEntity(any(CustomerDTO.class))).thenReturn(customer);
        when(repository.save(any(Customer.class))).thenReturn(customer);
        when(mapper.toDTO(any(Customer.class))).thenReturn(customerDTO);

        // When
        CustomerDTO result = customerService.create(customerDTO);

        // Then
        assertNotNull(result);
        assertEquals(customerDTO.getId(), result.getId());
        assertEquals(customerDTO.getName(), result.getName());
        verify(mapper, times(1)).toEntity(customerDTO);
        verify(repository, times(1)).save(customer);
        verify(mapper, times(1)).toDTO(customer);
    }

    @Test
    void testFindById_Success() {
        // Given
        Long customerId = 1L;
        when(repository.findById(customerId)).thenReturn(Optional.of(customer));
        when(mapper.toDTO(customer)).thenReturn(customerDTO);

        // When
        Optional<CustomerDTO> result = customerService.findById(customerId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(customerDTO.getId(), result.get().getId());
        verify(repository, times(1)).findById(customerId);
        verify(mapper, times(1)).toDTO(customer);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        Long customerId = 999L;
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        // When
        Optional<CustomerDTO> result = customerService.findById(customerId);

        // Then
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findById(customerId);
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void testFindAll_Success() {
        // Given
        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setStatus(StatusType.ACTIVE);
        
        List<Customer> customers = Arrays.asList(customer, customer2);
        when(repository.findAllByStatus(StatusType.ACTIVE)).thenReturn(customers);
        when(mapper.toDTO(customer)).thenReturn(customerDTO);
        
        CustomerDTO customerDTO2 = new CustomerDTO();
        customerDTO2.setId(2L);
        when(mapper.toDTO(customer2)).thenReturn(customerDTO2);

        // When
        List<CustomerDTO> result = customerService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAllByStatus(StatusType.ACTIVE);
        verify(mapper, times(2)).toDTO(any(Customer.class));
    }

    @Test
    void testUpdate_CustomerNotFound_ThrowsException() {
        // Given
        Long customerId = 999L;
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.update(customerId, customerDTO);
        });
        verify(repository, times(1)).findById(customerId);
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdate_CustomerDeleted_ThrowsException() {
        // Given
        Long customerId = 1L;
        CustomerDTO deletedCustomerDTO = new CustomerDTO();
        deletedCustomerDTO.setId(customerId);
        deletedCustomerDTO.setStatus(StatusType.DELETED);
        
        Customer deletedCustomer = new Customer();
        deletedCustomer.setId(customerId);
        deletedCustomer.setStatus(StatusType.DELETED);
        
        when(repository.findById(customerId)).thenReturn(Optional.of(deletedCustomer));
        when(mapper.toDTO(deletedCustomer)).thenReturn(deletedCustomerDTO);

        // When & Then
        assertThrows(CustomerDeletedException.class, () -> {
            customerService.update(customerId, customerDTO);
        });
        verify(repository, times(1)).findById(customerId);
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdate_Success() {
        // Given
        Long customerId = 1L;
        CustomerDTO updatedDTO = new CustomerDTO();
        updatedDTO.setName("Juan Perez");
        updatedDTO.setStatus(StatusType.ACTIVE);
        
        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(customerId);
        updatedCustomer.setName("Juan Perez");
        updatedCustomer.setStatus(StatusType.ACTIVE);
        
        when(repository.findById(customerId)).thenReturn(Optional.of(customer));
        when(mapper.toDTO(customer)).thenReturn(customerDTO);
        when(mapper.toEntity(any(CustomerDTO.class))).thenReturn(updatedCustomer);
        when(repository.save(any(Customer.class))).thenReturn(updatedCustomer);
        when(mapper.toDTO(updatedCustomer)).thenReturn(updatedDTO);

        // When
        CustomerDTO result = customerService.update(customerId, updatedDTO);

        // Then
        assertNotNull(result);
        assertEquals("Juan Perez", result.getName());
        verify(repository, times(1)).findById(customerId);
        verify(repository, times(1)).save(any(Customer.class));
    }

    @Test
    void testDelete_Success() {
        // Given
        Long customerId = 1L;
        CustomerDTO deletedDTO = new CustomerDTO();
        deletedDTO.setId(customerId);
        deletedDTO.setStatus(StatusType.DELETED);
        
        Customer deletedCustomer = new Customer();
        deletedCustomer.setId(customerId);
        deletedCustomer.setStatus(StatusType.DELETED);
        
        when(repository.findById(customerId)).thenReturn(Optional.of(customer));
        when(mapper.toDTO(customer)).thenReturn(customerDTO);
        when(mapper.toEntity(any(CustomerDTO.class))).thenReturn(deletedCustomer);
        when(repository.save(any(Customer.class))).thenReturn(deletedCustomer);
        when(mapper.toDTO(deletedCustomer)).thenReturn(deletedDTO);

        // When
        customerService.delete(customerId);

        // Then
        verify(repository, times(1)).findById(customerId);
        verify(repository, times(1)).save(any(Customer.class));
    }

    @Test
    void testDelete_CustomerNotFound_ThrowsException() {
        // Given
        Long customerId = 999L;
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.delete(customerId);
        });
        verify(repository, times(1)).findById(customerId);
        verify(repository, never()).save(any());
    }
}

