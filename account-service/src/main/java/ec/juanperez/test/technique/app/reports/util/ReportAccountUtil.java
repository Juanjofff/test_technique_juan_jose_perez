package ec.juanperez.test.technique.app.reports.util;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.dto.CustomerDTO;
import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ReportAccountUtil {
    
    public byte[] generateExcelReport(
            CustomerDTO customer,
            List<AccountDTO> accounts,
            Map<String, List<MovementDTO>> movementsMap,
            Map<Long, BigDecimal> accountBalances,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Estado de Cuenta");
            
            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // Información del Cliente
            rowNum = addCustomerInfo(sheet, customer, headerStyle, dataStyle, rowNum);
            
            // Período
            rowNum = addPeriodInfo(sheet, startTime, endTime, headerStyle, dataStyle, rowNum);
            
            // Para cada cuenta
            for (AccountDTO account : accounts) {
                BigDecimal currentBalance = accountBalances.getOrDefault(
                        account.getId(), 
                        account.getInitialBalance());
                
                rowNum = addAccountSection(
                        sheet, 
                        account, 
                        currentBalance, 
                        movementsMap, 
                        headerStyle, 
                        dataStyle, 
                        rowNum);
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Escribir a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        return headerStyle;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        return dataStyle;
    }
    
    private int addCustomerInfo(Sheet sheet, CustomerDTO customer, CellStyle headerStyle, CellStyle dataStyle, int rowNum) {
        Row customerRow = sheet.createRow(rowNum++);
        Cell customerLabelCell = customerRow.createCell(0);
        customerLabelCell.setCellValue("Cliente:");
        customerLabelCell.setCellStyle(headerStyle);
        Cell customerValueCell = customerRow.createCell(1);
        customerValueCell.setCellValue(customer.getName());
        customerValueCell.setCellStyle(dataStyle);
        
        Row identificationRow = sheet.createRow(rowNum++);
        Cell identificationLabelCell = identificationRow.createCell(0);
        identificationLabelCell.setCellValue("Identificación:");
        identificationLabelCell.setCellStyle(headerStyle);
        Cell identificationValueCell = identificationRow.createCell(1);
        identificationValueCell.setCellValue(customer.getIdentification());
        identificationValueCell.setCellStyle(dataStyle);
        
        return rowNum + 1; // Espacio
    }
    
    private int addPeriodInfo(Sheet sheet, LocalDateTime startTime, LocalDateTime endTime, 
                             CellStyle headerStyle, CellStyle dataStyle, int rowNum) {
        Row periodRow = sheet.createRow(rowNum++);
        Cell periodLabelCell = periodRow.createCell(0);
        periodLabelCell.setCellValue("Período:");
        periodLabelCell.setCellStyle(headerStyle);
        Cell periodValueCell = periodRow.createCell(1);
        periodValueCell.setCellValue(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                " al " + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        periodValueCell.setCellStyle(dataStyle);
        
        return rowNum + 1; // Espacio
    }
    
    private int addAccountSection(Sheet sheet, AccountDTO account, BigDecimal currentBalance,
                                 Map<String, List<MovementDTO>> movementsMap,
                                 CellStyle headerStyle, CellStyle dataStyle, int rowNum) {
        // Encabezado de cuenta
        Row accountHeaderRow = sheet.createRow(rowNum++);
        Cell accountHeaderCell = accountHeaderRow.createCell(0);
        accountHeaderCell.setCellValue("Cuenta: " + account.getNumber() + " - " + account.getAccountType() + 
                " | Saldo: " + currentBalance);
        accountHeaderCell.setCellStyle(headerStyle);
        
        // Encabezados de movimientos
        Row movementHeaderRow = sheet.createRow(rowNum++);
        String[] headers = {"Fecha", "Tipo", "Valor", "Saldo"};
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = movementHeaderRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
        
        // Movimientos de la cuenta
        String accountKey = account.getNumber() + "-" + account.getAccountType();
        List<MovementDTO> movements = movementsMap.getOrDefault(accountKey, List.of());
        
        for (MovementDTO movement : movements) {
            Row movementRow = sheet.createRow(rowNum++);
            movementRow.createCell(0).setCellValue(
                    movement.getFechaMovimiento().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            movementRow.createCell(1).setCellValue(movement.getMovementType().toString());
            movementRow.createCell(2).setCellValue(movement.getValue().doubleValue());
            movementRow.createCell(3).setCellValue(movement.getBalance().doubleValue());
            
            for (int i = 0; i < headers.length; i++) {
                movementRow.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        return rowNum + 1; // Espacio entre cuentas
    }
}
