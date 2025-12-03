package ec.juanperez.test.technique.repository;

import ec.juanperez.test.technique.model.CustomerReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerReferenceRepository extends JpaRepository<CustomerReference, Long> {
}

