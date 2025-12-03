package ec.juanperez.test.technique.app.reports.service.impl;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.accounts.service.AccountService;
import ec.juanperez.test.technique.app.customers.exception.CustomerNotFoundException;
import ec.juanperez.test.technique.app.customers.service.CustomerService;
import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import ec.juanperez.test.technique.app.movements.service.MovementService;
import ec.juanperez.test.technique.app.reports.dto.ReportAccountDTO;
import ec.juanperez.test.technique.app.reports.service.ReportService;
import ec.juanperez.test.technique.app.reports.util.ReportAccountUtil;
import ec.juanperez.test.technique.app.person.dto.PersonDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements ReportService {

    private final CustomerService customerService;
    private final MovementService movementService;
    private final AccountService accountService;
    private final ReportAccountUtil reportAccountUtil;
    
    @Override
    public ReportAccountDTO getAccountStatementByCustomerIdAndDates(Long customerId, LocalDateTime startTime, LocalDateTime endTime) {
        ReportAccountDTO report = new ReportAccountDTO();
        Optional<PersonDTO> optionalPersonDTO = this.customerService.findByIdReport(customerId);
        if (optionalPersonDTO.isEmpty()){
            log.error("Customer not found with id: {}", customerId);
            throw new CustomerNotFoundException(customerId);
        }
        report.setCustomer(optionalPersonDTO.get());
        report.setMovements(this.movementService.getMovementsByCustomerIdAndDates(customerId, startTime, endTime));
        return report;
    }

    @Override
    public byte[] generateExcelReport(Long customerId, LocalDateTime startTime, LocalDateTime endTime) {
        Optional<PersonDTO> optionalPersonDTO = this.customerService.findByIdReport(customerId);
        if (optionalPersonDTO.isEmpty()){
            log.error("Customer not found with id: {}", customerId);
            throw new CustomerNotFoundException(customerId);
        }
        
        PersonDTO customer = optionalPersonDTO.get();
        List<AccountDTO> accounts = this.accountService.findAllByCustomerId(customerId);
        Map<String, List<MovementDTO>> movementsMap =
                this.movementService.getMovementsByCustomerIdAndDates(customerId, startTime, endTime);
        
        Map<Long, BigDecimal> accountBalances = new HashMap<>();
        for (AccountDTO account : accounts) {
            Optional<BigDecimal> optionalBalance = this.movementService.getCurrentBalance(account.getId());
            BigDecimal currentBalance = optionalBalance.orElse(account.getInitialBalance());
            accountBalances.put(account.getId(), currentBalance);
        }
        
        return this.reportAccountUtil.generateExcelReport(
                customer, 
                accounts, 
                movementsMap, 
                accountBalances, 
                startTime, 
                endTime);
    }
}
