package ec.juanperez.test.technique.app.movements.service.impl;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.accounts.exception.AccountNotFoundException;
import ec.juanperez.test.technique.app.accounts.service.AccountService;
import ec.juanperez.test.technique.app.movements.enums.MovementType;
import ec.juanperez.test.technique.app.customers.exception.CustomerNotFoundException;
import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import ec.juanperez.test.technique.app.movements.exceptions.MovementBalanceException;
import ec.juanperez.test.technique.app.movements.exceptions.MovementValueException;
import ec.juanperez.test.technique.app.movements.exceptions.MovementNotFoundException;
import ec.juanperez.test.technique.app.movements.mapper.MovementMapper;
import ec.juanperez.test.technique.app.movements.model.Movements;
import ec.juanperez.test.technique.app.movements.repository.MovementRepository;
import ec.juanperez.test.technique.app.movements.service.MovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class MovementServiceImpl implements MovementService {

    private final MovementRepository repository;
    private final MovementMapper mapper;
    private final AccountService accountService;


    @Override
    public MovementDTO create(MovementDTO movementDTO) {
        if (movementDTO.getValue().compareTo(BigDecimal.ZERO) <= 0){
            log.error("Movement value is less than or equal to zero");
            throw new MovementValueException(movementDTO.getValue());
        }
        return this.save(movementDTO);
    }

    @Override
    public MovementDTO update(Long idMovement, MovementDTO movementDTO) {
        Optional<MovementDTO> optionalMovementDTO = this.findById(idMovement);
        if (optionalMovementDTO.isEmpty()) {
            log.error("Movement not found with id: {}", idMovement);
            throw new MovementNotFoundException(idMovement);
        }

        movementDTO.setId(idMovement);
        return this.save(movementDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MovementDTO> findById(Long id) {
        Optional<Movements> optionalMovement = this.repository.findByIdWithAccount(id);
        return optionalMovement.map(this.mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementDTO> findAll() {
        return this.repository.findAllWithAccount().stream().map(this.mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        Optional<MovementDTO> optionalMovementDTO = this.findById(id);
        if (optionalMovementDTO.isEmpty()) {
            log.error("Movement not found with id: {}", id);
            throw new CustomerNotFoundException(id);
        }
        this.repository.deleteById(id);
    }

    @Override
    public MovementDTO registerMovementByType(Long accountId, MovementType type, BigDecimal value) {
        Optional<AccountDTO> optionalAccountDTO = this.accountService.findById(accountId);
        if(optionalAccountDTO.isEmpty()){
            log.error("Account not found with id: {}", accountId);
            throw new AccountNotFoundException(accountId);
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0){
            log.error("Movement value is less than or equal to zero");
            throw new MovementValueException(value);
        }
        AccountDTO accountDTO = optionalAccountDTO.get();
        return this.createMovementByAccountAndType(accountDTO, type, value);
    }

    @Override
    public Map<String, List<MovementDTO>> getMovementsByCustomerIdAndDates(Long customerId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AccountDTO> accounts = this.accountService.findAllByCustomerId(customerId);
        Map<String, List<MovementDTO>> movements = new LinkedHashMap<>();
        accounts.forEach(account -> {
            String key = account.getNumber() + "-"+account.getAccountType();
            List<MovementDTO> movementsByAccount = this.repository.findMovementsByAccount(account.getId(), startTime, endTime);
            movements.put(key, movementsByAccount);
        });
        return movements;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BigDecimal> getCurrentBalance(Long accountId) {
        return this.repository.balanceLastMovement(accountId);
    }


    private MovementDTO save(MovementDTO movementDTO){
        Movements movement = this.mapper.toEntity(movementDTO);
        Movements movementSaved = this.repository.save(movement);
        // Store accountNumber before reloading in case mapper doesn't set it
        String accountNumberToPreserve = movementDTO.getAccountNumber();
        
        Optional<MovementDTO> optionalMovementDTO = this.findById(movementSaved.getId());
        MovementDTO result = optionalMovementDTO.orElseGet(() -> {
            log.error("Movement not found after save with id: {}", movementSaved.getId());
            throw new MovementNotFoundException(movementSaved.getId());
        });
        
        // Ensure accountNumber is set - preserve it if it was set before save, or get it from accountService
        if (result.getAccountNumber() == null) {
            if (accountNumberToPreserve != null) {
                result.setAccountNumber(accountNumberToPreserve);
            } else if (movementDTO.getAccountId() != null) {
                Optional<AccountDTO> accountDTO = this.accountService.findById(movementDTO.getAccountId());
                if (accountDTO.isPresent() && accountDTO.get().getNumber() != null) {
                    result.setAccountNumber(accountDTO.get().getNumber());
                    log.debug("Manually set accountNumber for movement id: {} from accountService", movementSaved.getId());
                }
            }
        }
        
        return result;
    }

    private MovementDTO createMovementByAccountAndType(AccountDTO accountDTO, MovementType type, BigDecimal value){
        MovementDTO movementDTO = new MovementDTO();
        movementDTO.setAccountId(accountDTO.getId());
        movementDTO.setMovementType(type);
        movementDTO.setValue(value);
        movementDTO.setFechaMovimiento(LocalDateTime.now());
        movementDTO.setAccountNumber(accountDTO.getNumber());

        Optional<BigDecimal> optionalBalance = this.repository.balanceLastMovement(accountDTO.getId());
        BigDecimal balance = optionalBalance.orElseGet(accountDTO::getInitialBalance);

        balance = switch (type) {
            case DEBIT -> balance.subtract(value);
            case CREDIT -> balance.add(value);
        };

        if (balance.compareTo(BigDecimal.ZERO) < 0){
            log.error("Balance is less than zero");
            throw new MovementBalanceException();
        }
        movementDTO.setBalance(balance);
        return this.save(movementDTO);
    }

}
