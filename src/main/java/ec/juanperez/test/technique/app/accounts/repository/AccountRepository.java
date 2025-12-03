package ec.juanperez.test.technique.app.accounts.repository;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.accounts.model.Account;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(" select a from Account a join fetch a.customer where a.status = :status")
    List<Account> findAllByStatus(@Param("status") StatusType status);

    @Query(" select new ec.juanperez.test.technique.app.accounts.dto.AccountDTO( " +
            " a.id, " +
            " a.number, " +
            " a.accountType " +
            ") " +
            " from Account a " +
            " where a.customer.id = :customerId " +
            " and a.status = :status " +
            " order by a.accountType, a.number ")
    List<AccountDTO> findAllByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                                  @Param("status") StatusType status);

    @Query(" select a from Account a join fetch a.customer where a.id = :id ")
    Optional<Account> findByIdWithCustomer(@Param("id") Long id);
}
