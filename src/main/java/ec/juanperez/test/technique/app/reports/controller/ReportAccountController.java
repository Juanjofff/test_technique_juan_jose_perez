package ec.juanperez.test.technique.app.reports.controller;

import ec.juanperez.test.technique.app.reports.dto.ReportAccountDTO;
import ec.juanperez.test.technique.app.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Reports account management API")
public class ReportAccountController {

    private final ReportService reportService;

    @Operation(summary = "Report Account Statement", description = "Generate account statement report in JSON or Excel format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ReportAccountDTO.class)),
                            @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    }),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid date format or parameters")
    })
    @GetMapping("/{client-id}")
    public Mono<ResponseEntity<?>> findReportByCustomer(
            @Parameter(description = "Customer ID", required = true) @PathVariable("client-id") Long clientId,
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true) 
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true) 
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "Format: json or excel (default: json)") 
            @RequestParam(value = "format", defaultValue = "json") String format) {
        log.info("Finding report by customer: {} from {} to {} in format: {}", clientId, startDate, endDate, format);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return Mono.fromCallable(() -> {
            if ("excel".equalsIgnoreCase(format)) {
                byte[] excelBytes = this.reportService.generateExcelReport(clientId, startDateTime, endDateTime);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers.setContentDispositionFormData("attachment", "account_statement_" + clientId + ".xlsx");
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(excelBytes);
            } else {
                ReportAccountDTO report = this.reportService.getAccountStatementByCustomerIdAndDates(clientId, startDateTime, endDateTime);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(report);
            }
        })
        .onErrorResume(e -> {
            log.error("Error finding report by customer: {} from {} to {}", clientId, startDate, endDate, e);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        });
    }
}
