package ec.juanperez.test.technique.app.customers.service.impl;

import ec.juanperez.test.technique.app.common.enums.StatusType;
import ec.juanperez.test.technique.app.customers.dto.CustomerDTO;
import ec.juanperez.test.technique.app.customers.exception.CustomerDeletedException;
import ec.juanperez.test.technique.app.customers.exception.CustomerNotFoundException;
import ec.juanperez.test.technique.app.customers.mapper.CustomerMapper;
import ec.juanperez.test.technique.app.customers.model.Customer;
import ec.juanperez.test.technique.app.customers.repository.CustomerRepository;
import ec.juanperez.test.technique.app.customers.service.CustomerService;
import ec.juanperez.test.technique.app.person.dto.PersonDTO;
import ec.juanperez.test.technique.events.CustomerCreatedEvent;
import ec.juanperez.test.technique.events.CustomerDeletedEvent;
import ec.juanperez.test.technique.events.CustomerUpdatedEvent;
import ec.juanperez.test.technique.kafka.CustomerEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final CustomerEventProducer eventProducer;

    @Override
    public CustomerDTO create(CustomerDTO customerDTO) {
        return  this.save(customerDTO);
    }

    @Override
    public CustomerDTO update(Long idCustomer, CustomerDTO customerDTO) {
        Optional<CustomerDTO> optionalCustomerDTO = this.findById(idCustomer);
        if (optionalCustomerDTO.isEmpty()) {
            log.error("Customer not found with id: {}", idCustomer);
            throw new CustomerNotFoundException(idCustomer);
        }

        if (optionalCustomerDTO.get().getStatus() == StatusType.DELETED) {
            log.error("Customer with id: {} is deleted", idCustomer);
            throw new CustomerDeletedException(idCustomer);
        }

        customerDTO.setId(idCustomer);
        return this.save(customerDTO);
    }

    @Override
    public Optional<CustomerDTO> findById(Long id) {
        Optional<Customer> optionalCustomer = this.repository.findById(id);
        return optionalCustomer.map(this.mapper::toDTO);
    }

    @Override
    public Optional<PersonDTO> findByIdReport(Long id) {
        return this.repository.findPersonByCustomerId(id);
    }

    @Override
    public List<CustomerDTO> findAll() {
        return this.repository.findAllByStatus(StatusType.ACTIVE).stream()
            .map(this.mapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        Optional<CustomerDTO> optionalCustomerDTO = this.findById(id);
        if (optionalCustomerDTO.isEmpty()) {
            log.error("Customer not found with id: {}", id);
            throw new CustomerNotFoundException(id);
        }
        CustomerDTO customerDTO = optionalCustomerDTO.get();
        customerDTO.setStatus(StatusType.DELETED);
        this.save(customerDTO);
        
        // Publish delete event
        eventProducer.publishCustomerDeleted(new CustomerDeletedEvent(id));
    }

    private CustomerDTO save(CustomerDTO customerDTO){
        Customer customer = this.mapper.toEntity(customerDTO);
        boolean isNew = customer.getId() == null;
        customer = this.repository.save(customer);
        CustomerDTO savedDTO = this.mapper.toDTO(customer);
        
        // Publish events
        if (isNew) {
            eventProducer.publishCustomerCreated(new CustomerCreatedEvent(
                    savedDTO.getId(),
                    savedDTO.getName(),
                    savedDTO.getIdentification(),
                    savedDTO.getStatus().toString()
            ));
        } else {
            eventProducer.publishCustomerUpdated(new CustomerUpdatedEvent(
                    savedDTO.getId(),
                    savedDTO.getName(),
                    savedDTO.getIdentification(),
                    savedDTO.getStatus().toString()
            ));
        }
        
        return savedDTO;
    }

}
