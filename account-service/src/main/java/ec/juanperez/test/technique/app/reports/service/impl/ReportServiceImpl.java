package ec.juanperez.test.technique.app.reports.service.impl;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.accounts.service.AccountService;
import ec.juanperez.test.technique.app.dto.CustomerDTO;
import ec.juanperez.test.technique.app.exception.CustomerReferenceNotFoundException;
import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import ec.juanperez.test.technique.app.movements.service.MovementService;
import ec.juanperez.test.technique.app.reports.dto.ReportAccountDTO;
import ec.juanperez.test.technique.app.reports.service.ReportService;
import ec.juanperez.test.technique.app.reports.util.ReportAccountUtil;
import ec.juanperez.test.technique.model.CustomerReference;
import ec.juanperez.test.technique.repository.CustomerReferenceRepository;
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

    private final CustomerReferenceRepository customerReferenceRepository;
    private final MovementService movementService;
    private final AccountService accountService;
    private final ReportAccountUtil reportAccountUtil;
    
    @Override
    public ReportAccountDTO getAccountStatementByCustomerIdAndDates(Long customerId, LocalDateTime startTime, LocalDateTime endTime) {
        ReportAccountDTO report = new ReportAccountDTO();
        Optional<CustomerReference> optionalCustomerRef = this.customerReferenceRepository.findById(customerId);
        if (optionalCustomerRef.isEmpty()){
            log.error("Customer reference not found with id: {}", customerId);
            throw new CustomerReferenceNotFoundException(customerId);
        }
        
        CustomerReference customerRef = optionalCustomerRef.get();
        CustomerDTO customerDTO = new CustomerDTO(
            customerRef.getId(),
            customerRef.getName(),
            customerRef.getIdentification(),
            customerRef.getStatus().name()
        );
        report.setCustomer(customerDTO);
        report.setMovements(this.movementService.getMovementsByCustomerIdAndDates(customerId, startTime, endTime));
        return report;
    }

    @Override
    public byte[] generateExcelReport(Long customerId, LocalDateTime startTime, LocalDateTime endTime) {
        Optional<CustomerReference> optionalCustomerRef = this.customerReferenceRepository.findById(customerId);
        if (optionalCustomerRef.isEmpty()){
            log.error("Customer reference not found with id: {}", customerId);
            throw new CustomerReferenceNotFoundException(customerId);
        }
        
        CustomerReference customerRef = optionalCustomerRef.get();
        CustomerDTO customer = new CustomerDTO(
            customerRef.getId(),
            customerRef.getName(),
            customerRef.getIdentification(),
            customerRef.getStatus().name()
        );
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
