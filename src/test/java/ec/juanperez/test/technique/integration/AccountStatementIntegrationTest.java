package ec.juanperez.test.technique.integration;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.accounts.enums.AccountType;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import ec.juanperez.test.technique.app.customers.dto.CustomerDTO;
import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import ec.juanperez.test.technique.app.movements.dto.RegisterMovementRequest;
import ec.juanperez.test.technique.app.movements.enums.MovementType;
import ec.juanperez.test.technique.app.person.enums.GenderType;
import ec.juanperez.test.technique.app.reports.dto.ReportAccountDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.web-application-type=reactive"
        }
)
@AutoConfigureWebTestClient(timeout = "36000")
@ActiveProfiles("test")
@Transactional
class AccountStatementIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testCompleteFlow_CreateCustomer_Account_Movements_Report() {
        // Step 1: Create Customer
        CustomerDTO customer = createCustomer("Juan Pérez Test", "1234567890");
        validateCustomer(customer, "Juan Pérez Test", "1234567890");

        // Step 2: Create Account
        AccountDTO account = createAccount(customer.getId(), "123456789", AccountType.AHORROS, new BigDecimal("1000.00"));
        validateAccount(account, "123456789", new BigDecimal("1000.00"), customer.getId());

        // Step 3: Register Credit Movement
        MovementDTO creditMovement = registerCreditMovement(account.getId(), new BigDecimal("500.00"));
        validateCreditMovement(creditMovement, account.getId(), new BigDecimal("500.00"), new BigDecimal("1500.00"));

        // Step 4: Register Debit Movement
        MovementDTO debitMovement = registerDebitMovement(account.getId(), new BigDecimal("200.00"));
        validateDebitMovement(debitMovement, account.getId(), new BigDecimal("200.00"), new BigDecimal("1300.00"));

        // Step 5: Generate Report
        ReportAccountDTO report = generateReport(customer.getId());
        validateReport(report, customer.getName(), account.getNumber(), account.getAccountType());
    }

    private CustomerDTO createCustomer(String name, String identification) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName(name);
        customerDTO.setGender(GenderType.MALE);
        customerDTO.setIdentification(identification);
        customerDTO.setAddress("Test Address");
        customerDTO.setPhone("+593999999999");
        customerDTO.setPassword("password123");
        customerDTO.setStatus(StatusType.ACTIVE);

        return webTestClient.post()
                .uri("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(customerDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private void validateCustomer(CustomerDTO customer, String expectedName, String expectedIdentification) {
        assertNotNull(customer, "Customer should not be null");
        assertNotNull(customer.getId(), "Customer ID should not be null");
        assertEquals(expectedName, customer.getName(), "Customer name should match");
        assertEquals(expectedIdentification, customer.getIdentification(), "Customer identification should match");
    }

    private AccountDTO createAccount(Long customerId, String number, AccountType accountType, BigDecimal initialBalance) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setNumber(number);
        accountDTO.setAccountType(accountType);
        accountDTO.setInitialBalance(initialBalance);
        accountDTO.setStatus(StatusType.ACTIVE);
        accountDTO.setCustomerId(customerId);

        return webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(accountDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AccountDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private void validateAccount(AccountDTO account, String expectedNumber, BigDecimal expectedBalance, Long expectedCustomerId) {
        assertNotNull(account, "Account should not be null");
        assertNotNull(account.getId(), "Account ID should not be null");
        assertEquals(expectedNumber, account.getNumber(), "Account number should match");
        assertEquals(expectedBalance, account.getInitialBalance(), "Account initial balance should match");
        assertEquals(expectedCustomerId, account.getCustomerId(), "Account customer ID should match");
        assertNotNull(account.getCustomerName(), "Account customer name should not be null");
    }

    private MovementDTO registerCreditMovement(Long accountId, BigDecimal value) {
        RegisterMovementRequest request = new RegisterMovementRequest();
        request.setAccountId(accountId);
        request.setValue(value);

        return webTestClient.post()
                .uri("/movements/register/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(MovementDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private void validateCreditMovement(MovementDTO movement, Long expectedAccountId, BigDecimal expectedValue, BigDecimal expectedBalance) {
        assertNotNull(movement, "Credit movement should not be null");
        assertNotNull(movement.getId(), "Credit movement ID should not be null");
        assertEquals(MovementType.CREDIT, movement.getMovementType(), "Movement type should be CREDIT");
        assertEquals(expectedValue, movement.getValue(), "Movement value should match");
        assertEquals(expectedBalance, movement.getBalance(), "Movement balance should match");
        assertEquals(expectedAccountId, movement.getAccountId(), "Movement account ID should match");
    }

    private MovementDTO registerDebitMovement(Long accountId, BigDecimal value) {
        RegisterMovementRequest request = new RegisterMovementRequest();
        request.setAccountId(accountId);
        request.setValue(value);

        return webTestClient.post()
                .uri("/movements/register/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(MovementDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private void validateDebitMovement(MovementDTO movement, Long expectedAccountId, BigDecimal expectedValue, BigDecimal expectedBalance) {
        assertNotNull(movement, "Debit movement should not be null");
        assertNotNull(movement.getId(), "Debit movement ID should not be null");
        assertEquals(MovementType.DEBIT, movement.getMovementType(), "Movement type should be DEBIT");
        assertEquals(expectedValue, movement.getValue(), "Movement value should match");
        assertEquals(expectedBalance, movement.getBalance(), "Movement balance should match");
        assertEquals(expectedAccountId, movement.getAccountId(), "Movement account ID should match");
    }

    private ReportAccountDTO generateReport(Long customerId) {
        LocalDate today = LocalDate.now();
        String startDate = today.toString();
        String endDate = today.toString();

        return webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/reports/{client-id}")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build(customerId))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ReportAccountDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private void validateReport(ReportAccountDTO report, String expectedCustomerName, String accountNumber, AccountType accountType) {
        assertNotNull(report, "Report should not be null");
        assertNotNull(report.getCustomer(), "Report customer should not be null");
        assertEquals(expectedCustomerName, report.getCustomer().getName(), "Report customer name should match");
        assertNotNull(report.getMovements(), "Report movements should not be null");
        assertFalse(report.getMovements().isEmpty(), "Report movements should not be empty");

        String accountKey = accountNumber + "-" + accountType;
        assertTrue(report.getMovements().containsKey(accountKey), "Report should contain movements for account: " + accountKey);
        assertNotNull(report.getMovements().get(accountKey), "Report movements for account should not be null");
        assertTrue(report.getMovements().get(accountKey).size() >= 2, "Report should contain at least 2 movements");
    }

    @Test
    void testCompleteFlow_WithInsufficientBalance_ShouldFail() {
        // Step 1: Create Customer
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("Test Customer");
        customerDTO.setGender(GenderType.MALE);
        customerDTO.setIdentification("9876543210");
        customerDTO.setAddress("Test Address");
        customerDTO.setPhone("+593999999999");
        customerDTO.setPassword("password123");
        customerDTO.setStatus(StatusType.ACTIVE);

        CustomerDTO customer = webTestClient.post()
                .uri("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(customerDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(customer);

        // Step 2: Create Account with low balance
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setNumber("987654321");
        accountDTO.setAccountType(AccountType.CORRIENTE);
        accountDTO.setInitialBalance(new BigDecimal("100.00"));
        accountDTO.setStatus(StatusType.ACTIVE);
        accountDTO.setCustomerId(customer.getId());

        AccountDTO account = webTestClient.post()
                .uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(accountDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(account);

        // Step 3: Try to register Debit Movement with insufficient balance
        RegisterMovementRequest debitRequest = new RegisterMovementRequest();
        debitRequest.setAccountId(account.getId());
        debitRequest.setValue(new BigDecimal("500.00")); // More than available balance

        webTestClient.post()
                .uri("/movements/register/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(debitRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .value(response -> {
                    assertTrue(response.containsKey("message"));
                    String message = (String) response.get("message");
                    assertTrue(message.contains("Saldo no disponible") || message.contains("Balance"));
                });
    }
}

