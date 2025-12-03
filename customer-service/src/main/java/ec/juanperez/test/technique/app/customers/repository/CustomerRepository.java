package ec.juanperez.test.technique.app.customers.repository;

import ec.juanperez.test.technique.app.common.enums.StatusType;
import ec.juanperez.test.technique.app.customers.model.Customer;
import ec.juanperez.test.technique.app.person.dto.PersonDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByStatus(StatusType status);

    @Query("Select new ec.juanperez.test.technique.app.person.dto.PersonDTO( " +
            " c.name, " +
            " c.gender, " +
            " c.identification, " +
            " c.address, " +
            " c.phone " +
            " ) " +
            " from Customer c " +
            " where c.id = :id ")
    Optional<PersonDTO> findPersonByCustomerId(@Param("id") Long id);
}
