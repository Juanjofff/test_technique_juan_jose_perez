package ec.juanperez.test.technique.app.customers.service;

import ec.juanperez.test.technique.app.customers.dto.CustomerDTO;
import ec.juanperez.test.technique.app.person.dto.PersonDTO;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    CustomerDTO create(CustomerDTO customerDTO);
    CustomerDTO update(Long idCustomer, CustomerDTO customerDTO);
    Optional<CustomerDTO> findById(Long id);
    Optional<PersonDTO> findByIdReport(Long id);
    List<CustomerDTO> findAll();
    void delete(Long id);
}
