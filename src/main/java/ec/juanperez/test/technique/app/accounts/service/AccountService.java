package ec.juanperez.test.technique.app.accounts.service;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    AccountDTO create(AccountDTO accountDTO);
    AccountDTO update(Long accountId, AccountDTO accountDTO );
    Optional<AccountDTO> findById(Long id);
    List<AccountDTO> findAll();
    void delete(Long id);
    List<AccountDTO> findAllByCustomerId(Long customerId);
}
