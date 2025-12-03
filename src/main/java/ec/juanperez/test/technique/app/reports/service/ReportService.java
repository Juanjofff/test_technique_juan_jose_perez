package ec.juanperez.test.technique.app.reports.service;

import ec.juanperez.test.technique.app.reports.dto.ReportAccountDTO;

import java.time.LocalDateTime;

public interface ReportService {

    ReportAccountDTO getAccountStatementByCustomerIdAndDates(Long customerId, LocalDateTime startTime, LocalDateTime endTime);
    
    byte[] generateExcelReport(Long customerId, LocalDateTime startTime, LocalDateTime endTime);
}
