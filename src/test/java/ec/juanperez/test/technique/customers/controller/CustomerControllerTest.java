package ec.juanperez.test.technique.customers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.juanperez.test.technique.app.customers.controller.CustomerController;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import ec.juanperez.test.technique.app.customers.dto.CustomerDTO;
import ec.juanperez.test.technique.app.customers.service.CustomerService;
import ec.juanperez.test.technique.app.person.enums.GenderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
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
    void testFindAll_Success() {
        // Given
        CustomerDTO customerDTO2 = new CustomerDTO();
        customerDTO2.setId(2L);
        customerDTO2.setName("Juan José Pérez");
        customerDTO2.setStatus(StatusType.ACTIVE);
        
        List<CustomerDTO> customers = Arrays.asList(customerDTO, customerDTO2);
        when(customerService.findAll()).thenReturn(customers);

        // When & Then
        webTestClient.get()
                .uri("/customers")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CustomerDTO.class)
                .hasSize(2);

        verify(customerService, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        // Given
        Long customerId = 1L;
        when(customerService.findById(customerId)).thenReturn(Optional.of(customerDTO));

        // When & Then
        webTestClient.get()
                .uri("/customers/{id}", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDTO.class)
                .value(c -> {
                    assert c.getId().equals(customerId);
                    assert c.getName().equals("Juan Pérez");
                });

        verify(customerService, times(1)).findById(customerId);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        Long customerId = 999L;
        when(customerService.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        webTestClient.get()
                .uri("/customers/{id}", customerId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customerService, times(1)).findById(customerId);
    }

    @Test
    void testCreate_Success() {
        // Given
        CustomerDTO newCustomerDTO = new CustomerDTO();
        newCustomerDTO.setName("Silvana Quevedo");
        newCustomerDTO.setGender(GenderType.FEMALE);
        newCustomerDTO.setIdentification("9876543210");
        newCustomerDTO.setAddress("El Batán");
        newCustomerDTO.setPhone("+593984391740");
        newCustomerDTO.setPassword("newpassword");
        newCustomerDTO.setStatus(StatusType.ACTIVE);
        
        CustomerDTO savedCustomerDTO = new CustomerDTO();
        savedCustomerDTO.setId(3L);
        savedCustomerDTO.setName("Silvana Quevedo");
        savedCustomerDTO.setStatus(StatusType.ACTIVE);
        
        when(customerService.create(any(CustomerDTO.class))).thenReturn(savedCustomerDTO);

        // When & Then
        webTestClient.post()
                .uri("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newCustomerDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDTO.class)
                .value(c -> {
                    assert c.getId().equals(3L);
                    assert c.getName().equals("Silvana Quevedo");
                });

        verify(customerService, times(1)).create(any(CustomerDTO.class));
    }

    @Test
    void testUpdate_Success() {
        // Given
        Long customerId = 1L;
        CustomerDTO updatedDTO = new CustomerDTO();
        updatedDTO.setName("Juan Perez");
        updatedDTO.setStatus(StatusType.ACTIVE);
        
        CustomerDTO savedDTO = new CustomerDTO();
        savedDTO.setId(customerId);
        savedDTO.setName("Juan Perez");
        savedDTO.setStatus(StatusType.ACTIVE);
        
        when(customerService.update(eq(customerId), any(CustomerDTO.class))).thenReturn(savedDTO);

        // When & Then
        webTestClient.put()
                .uri("/customers/{id}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDTO.class)
                .value(c -> {
                    assert c.getId().equals(customerId);
                    assert c.getName().equals("Juan Perez");
                });

        verify(customerService, times(1)).update(eq(customerId), any(CustomerDTO.class));
    }
}

