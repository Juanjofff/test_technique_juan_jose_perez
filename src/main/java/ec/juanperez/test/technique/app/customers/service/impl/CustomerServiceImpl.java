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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

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
    }

    private CustomerDTO save(CustomerDTO customerDTO){
        Customer customer = this.mapper.toEntity(customerDTO);
        customer = this.repository.save(customer);
        return this.mapper.toDTO(customer);
    }

}
