package ec.juanperez.test.technique.app.accounts.service.impl;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.accounts.exception.AccountDeletedException;
import ec.juanperez.test.technique.app.accounts.exception.AccountNotFoundException;
import ec.juanperez.test.technique.app.accounts.mapper.AccountMapper;
import ec.juanperez.test.technique.app.accounts.model.Account;
import ec.juanperez.test.technique.app.accounts.repository.AccountRepository;
import ec.juanperez.test.technique.app.accounts.service.AccountService;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import ec.juanperez.test.technique.app.customers.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repository;
    private final AccountMapper mapper;

    @Override
    public AccountDTO create(AccountDTO accountDTO) {
        return this.save(accountDTO);
    }

    @Override
    public AccountDTO update(Long accountId, AccountDTO accountDTO) {
        Optional<AccountDTO> optionalAccountDTO = this.findById(accountId);
        if (optionalAccountDTO.isEmpty()) {
            log.error("Account not found with id: {}", accountId);
            throw new AccountNotFoundException(accountId);
        }

        if (optionalAccountDTO.get().getStatus() == StatusType.DELETED) {
            log.error("Account with id: {} is deleted", accountId);
            throw new AccountDeletedException(accountId);
        }

        accountDTO.setId(accountId);
        return this.save(accountDTO);
    }

    @Override
    public Optional<AccountDTO> findById(Long id) {
        Optional<Account> optionalAccount = this.repository.findByIdWithCustomer(id);
        return optionalAccount.map(this.mapper::toDto);
    }

    @Override
    public List<AccountDTO> findAll() {
        return this.repository.findAllByStatus(StatusType.ACTIVE)
                .stream().map(this.mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        Optional<AccountDTO> optionalAccountDTO = this.findById(id);
        if (optionalAccountDTO.isEmpty()) {
            log.error("Account not found with id: {}", id);
            throw new CustomerNotFoundException(id);
        }
        AccountDTO customerDTO = optionalAccountDTO.get();
        customerDTO.setStatus(StatusType.DELETED);
        this.save(customerDTO);
    }

    @Override
    public List<AccountDTO> findAllByCustomerId(Long customerId) {
        return this.repository.findAllByCustomerIdAndStatus(customerId, StatusType.ACTIVE);
    }

    private AccountDTO save(AccountDTO accountDTO){
        Account account = this.mapper.toEntity(accountDTO);
        Account accountSaved = this.repository.save(account);

        Optional<Account> optionalAccount = this.repository.findByIdWithCustomer(accountSaved.getId());
        return optionalAccount.map(this.mapper::toDto)
                .orElseThrow(() -> new AccountNotFoundException(accountSaved.getId()));
    }
}
