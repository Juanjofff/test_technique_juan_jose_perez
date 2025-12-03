package ec.juanperez.test.technique.movements.service.impl;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.accounts.enums.AccountType;
import ec.juanperez.test.technique.app.accounts.exception.AccountNotFoundException;
import ec.juanperez.test.technique.app.accounts.service.AccountService;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import ec.juanperez.test.technique.app.movements.enums.MovementType;
import ec.juanperez.test.technique.app.movements.exceptions.MovementBalanceException;
import ec.juanperez.test.technique.app.movements.exceptions.MovementNotFoundException;
import ec.juanperez.test.technique.app.movements.exceptions.MovementValueException;
import ec.juanperez.test.technique.app.movements.mapper.MovementMapper;
import ec.juanperez.test.technique.app.movements.model.Movements;
import ec.juanperez.test.technique.app.movements.repository.MovementRepository;
import ec.juanperez.test.technique.app.movements.service.impl.MovementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementServiceImplTest {

    @Mock
    private MovementRepository repository;

    @Mock
    private MovementMapper mapper;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private MovementServiceImpl movementService;

    private AccountDTO accountDTO;
    private MovementDTO movementDTO;
    private Movements movement;

    @BeforeEach
    void setUp() {
        accountDTO = new AccountDTO();
        accountDTO.setId(1L);
        accountDTO.setNumber("478758");
        accountDTO.setAccountType(AccountType.AHORROS);
        accountDTO.setInitialBalance(new BigDecimal("1000.00"));
        accountDTO.setStatus(StatusType.ACTIVE);

        movementDTO = new MovementDTO();
        movementDTO.setId(1L);
        movementDTO.setAccountId(1L);
        movementDTO.setMovementType(MovementType.CREDIT);
        movementDTO.setValue(new BigDecimal("100.00"));
        movementDTO.setFechaMovimiento(LocalDateTime.now());
        movementDTO.setBalance(new BigDecimal("1100.00"));

        movement = new Movements();
        movement.setId(1L);
        movement.setValue(new BigDecimal("100.00"));
        movement.setBalance(new BigDecimal("1100.00"));
    }

    @Test
    void testRegisterMovementByType_Credit_Success() {
        // Given
        Long accountId = 1L;
        MovementType type = MovementType.CREDIT;
        BigDecimal value = new BigDecimal("100.00");
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal expectedBalance = new BigDecimal("1100.00");

        when(accountService.findById(accountId)).thenReturn(Optional.of(accountDTO));
        when(repository.balanceLastMovement(accountId)).thenReturn(Optional.empty());
        when(mapper.toEntity(any(MovementDTO.class))).thenReturn(movement);
        when(repository.save(any(Movements.class))).thenReturn(movement);
        when(mapper.toDTO(any(Movements.class))).thenReturn(movementDTO);

        // When
        MovementDTO result = movementService.registerMovementByType(accountId, type, value);

        // Then
        assertNotNull(result);
        assertEquals(MovementType.CREDIT, result.getMovementType());
        assertEquals(value, result.getValue());
        verify(accountService, times(1)).findById(accountId);
        verify(repository, times(1)).balanceLastMovement(accountId);
        verify(repository, times(1)).save(any(Movements.class));
    }

    @Test
    void testRegisterMovementByType_Debit_Success() {
        // Given
        Long accountId = 1L;
        MovementType type = MovementType.DEBIT;
        BigDecimal value = new BigDecimal("200.00");
        BigDecimal currentBalance = new BigDecimal("1000.00");
        BigDecimal expectedBalance = new BigDecimal("800.00");

        MovementDTO debitMovementDTO = new MovementDTO();
        debitMovementDTO.setId(2L);
        debitMovementDTO.setMovementType(MovementType.DEBIT);
        debitMovementDTO.setValue(value);
        debitMovementDTO.setBalance(expectedBalance);

        when(accountService.findById(accountId)).thenReturn(Optional.of(accountDTO));
        when(repository.balanceLastMovement(accountId)).thenReturn(Optional.of(currentBalance));
        when(mapper.toEntity(any(MovementDTO.class))).thenReturn(movement);
        when(repository.save(any(Movements.class))).thenReturn(movement);
        when(mapper.toDTO(any(Movements.class))).thenReturn(debitMovementDTO);

        // When
        MovementDTO result = movementService.registerMovementByType(accountId, type, value);

        // Then
        assertNotNull(result);
        assertEquals(MovementType.DEBIT, result.getMovementType());
        assertEquals(value, result.getValue());
        verify(accountService, times(1)).findById(accountId);
        verify(repository, times(1)).balanceLastMovement(accountId);
        verify(repository, times(1)).save(any(Movements.class));
    }

    @Test
    void testRegisterMovementByType_CreditWithPreviousBalance_Success() {
        // Given
        Long accountId = 1L;
        MovementType type = MovementType.CREDIT;
        BigDecimal value = new BigDecimal("150.00");
        BigDecimal previousBalance = new BigDecimal("500.00");
        BigDecimal expectedBalance = new BigDecimal("650.00");

        when(accountService.findById(accountId)).thenReturn(Optional.of(accountDTO));
        when(repository.balanceLastMovement(accountId)).thenReturn(Optional.of(previousBalance));
        when(mapper.toEntity(any(MovementDTO.class))).thenReturn(movement);
        when(repository.save(any(Movements.class))).thenReturn(movement);
        when(mapper.toDTO(any(Movements.class))).thenReturn(movementDTO);

        // When
        MovementDTO result = movementService.registerMovementByType(accountId, type, value);

        // Then
        assertNotNull(result);
        verify(repository, times(1)).balanceLastMovement(accountId);
        verify(repository, times(1)).save(any(Movements.class));
    }

    @Test
    void testRegisterMovementByType_AccountNotFound_ThrowsException() {
        // Given
        Long accountId = 999L;
        MovementType type = MovementType.CREDIT;
        BigDecimal value = new BigDecimal("100.00");

        when(accountService.findById(accountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AccountNotFoundException.class, () -> {
            movementService.registerMovementByType(accountId, type, value);
        });
        verify(accountService, times(1)).findById(accountId);
        verify(repository, never()).balanceLastMovement(any());
        verify(repository, never()).save(any());
    }

    @Test
    void testRegisterMovementByType_ValueZero_ThrowsException() {
        // Given
        Long accountId = 1L;
        MovementType type = MovementType.CREDIT;
        BigDecimal value = BigDecimal.ZERO;

        when(accountService.findById(accountId)).thenReturn(Optional.of(accountDTO));

        // When & Then
        assertThrows(MovementValueException.class, () -> {
            movementService.registerMovementByType(accountId, type, value);
        });
        verify(accountService, times(1)).findById(accountId);
        verify(repository, never()).balanceLastMovement(any());
        verify(repository, never()).save(any());
    }

    @Test
    void testRegisterMovementByType_ValueNegative_ThrowsException() {
        // Given
        Long accountId = 1L;
        MovementType type = MovementType.CREDIT;
        BigDecimal value = new BigDecimal("-50.00");

        when(accountService.findById(accountId)).thenReturn(Optional.of(accountDTO));

        // When & Then
        assertThrows(MovementValueException.class, () -> {
            movementService.registerMovementByType(accountId, type, value);
        });
        verify(accountService, times(1)).findById(accountId);
        verify(repository, never()).balanceLastMovement(any());
        verify(repository, never()).save(any());
    }

    @Test
    void testRegisterMovementByType_DebitInsufficientBalance_ThrowsException() {
        // Given
        Long accountId = 1L;
        MovementType type = MovementType.DEBIT;
        BigDecimal value = new BigDecimal("1500.00");
        BigDecimal currentBalance = new BigDecimal("1000.00");

        when(accountService.findById(accountId)).thenReturn(Optional.of(accountDTO));
        when(repository.balanceLastMovement(accountId)).thenReturn(Optional.of(currentBalance));

        // When & Then
        assertThrows(MovementBalanceException.class, () -> {
            movementService.registerMovementByType(accountId, type, value);
        });
        verify(accountService, times(1)).findById(accountId);
        verify(repository, times(1)).balanceLastMovement(accountId);
        verify(repository, never()).save(any());
    }

    @Test
    void testRegisterMovementByType_DebitExactBalance_Success() {
        // Given
        Long accountId = 1L;
        MovementType type = MovementType.DEBIT;
        BigDecimal value = new BigDecimal("1000.00");
        BigDecimal currentBalance = new BigDecimal("1000.00");
        BigDecimal expectedBalance = BigDecimal.ZERO;

        MovementDTO debitMovementDTO = new MovementDTO();
        debitMovementDTO.setId(3L);
        debitMovementDTO.setMovementType(MovementType.DEBIT);
        debitMovementDTO.setValue(value);
        debitMovementDTO.setBalance(expectedBalance);

        when(accountService.findById(accountId)).thenReturn(Optional.of(accountDTO));
        when(repository.balanceLastMovement(accountId)).thenReturn(Optional.of(currentBalance));
        when(mapper.toEntity(any(MovementDTO.class))).thenReturn(movement);
        when(repository.save(any(Movements.class))).thenReturn(movement);
        when(mapper.toDTO(any(Movements.class))).thenReturn(debitMovementDTO);

        // When
        MovementDTO result = movementService.registerMovementByType(accountId, type, value);

        // Then
        assertNotNull(result);
        assertEquals(MovementType.DEBIT, result.getMovementType());
        verify(repository, times(1)).save(any(Movements.class));
    }

    @Test
    void testRegisterMovementByType_DebitWithInitialBalance_Success() {
        // Given
        Long accountId = 1L;
        MovementType type = MovementType.DEBIT;
        BigDecimal value = new BigDecimal("300.00");
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal expectedBalance = new BigDecimal("700.00");

        MovementDTO debitMovementDTO = new MovementDTO();
        debitMovementDTO.setId(3L);
        debitMovementDTO.setMovementType(MovementType.DEBIT);
        debitMovementDTO.setValue(value);
        debitMovementDTO.setBalance(expectedBalance);

        when(accountService.findById(accountId)).thenReturn(Optional.of(accountDTO));
        when(repository.balanceLastMovement(accountId)).thenReturn(Optional.empty());
        when(mapper.toEntity(any(MovementDTO.class))).thenReturn(movement);
        when(repository.save(any(Movements.class))).thenReturn(movement);
        when(mapper.toDTO(any(Movements.class))).thenReturn(debitMovementDTO);

        // When
        MovementDTO result = movementService.registerMovementByType(accountId, type, value);

        // Then
        assertNotNull(result);
        assertEquals(MovementType.DEBIT, result.getMovementType());
        assertEquals(value, result.getValue());
        verify(repository, times(1)).balanceLastMovement(accountId);
        verify(repository, times(1)).save(any(Movements.class));
    }

    @Test
    void testCreate_ValueZero_ThrowsException() {
        // Given
        MovementDTO invalidMovementDTO = new MovementDTO();
        invalidMovementDTO.setValue(BigDecimal.ZERO);

        // When & Then
        assertThrows(MovementValueException.class, () -> {
            movementService.create(invalidMovementDTO);
        });
        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_ValueNegative_ThrowsException() {
        // Given
        MovementDTO invalidMovementDTO = new MovementDTO();
        invalidMovementDTO.setValue(new BigDecimal("-10.00"));

        // When & Then
        assertThrows(MovementValueException.class, () -> {
            movementService.create(invalidMovementDTO);
        });
        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_Success() {
        // Given
        MovementDTO validMovementDTO = new MovementDTO();
        validMovementDTO.setValue(new BigDecimal("100.00"));
        validMovementDTO.setMovementType(MovementType.CREDIT);

        when(mapper.toEntity(validMovementDTO)).thenReturn(movement);
        when(repository.save(any(Movements.class))).thenReturn(movement);
        when(mapper.toDTO(movement)).thenReturn(movementDTO);

        // When
        MovementDTO result = movementService.create(validMovementDTO);

        // Then
        assertNotNull(result);
        verify(mapper, times(1)).toEntity(validMovementDTO);
        verify(repository, times(1)).save(any(Movements.class));
        verify(mapper, times(1)).toDTO(movement);
    }

    @Test
    void testFindById_Success() {
        // Given
        Long movementId = 1L;
        when(repository.findById(movementId)).thenReturn(Optional.of(movement));
        when(mapper.toDTO(movement)).thenReturn(movementDTO);

        // When
        Optional<MovementDTO> result = movementService.findById(movementId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(movementDTO.getId(), result.get().getId());
        verify(repository, times(1)).findById(movementId);
        verify(mapper, times(1)).toDTO(movement);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        Long movementId = 999L;
        when(repository.findById(movementId)).thenReturn(Optional.empty());

        // When
        Optional<MovementDTO> result = movementService.findById(movementId);

        // Then
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findById(movementId);
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void testUpdate_MovementNotFound_ThrowsException() {
        // Given
        Long movementId = 999L;
        MovementDTO movementDTOToUpdate = new MovementDTO();
        movementDTOToUpdate.setValue(new BigDecimal("200.00"));

        when(repository.findById(movementId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MovementNotFoundException.class, () -> {
            movementService.update(movementId, movementDTOToUpdate);
        });
        verify(repository, times(1)).findById(movementId);
        verify(repository, never()).save(any());
    }

    @Test
    void testGetCurrentBalance_Success() {
        // Given
        Long accountId = 1L;
        BigDecimal expectedBalance = new BigDecimal("500.00");
        when(repository.balanceLastMovement(accountId)).thenReturn(Optional.of(expectedBalance));

        // When
        Optional<BigDecimal> result = movementService.getCurrentBalance(accountId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedBalance, result.get());
        verify(repository, times(1)).balanceLastMovement(accountId);
    }

    @Test
    void testGetCurrentBalance_NoMovements_ReturnsEmpty() {
        // Given
        Long accountId = 1L;
        when(repository.balanceLastMovement(accountId)).thenReturn(Optional.empty());

        // When
        Optional<BigDecimal> result = movementService.getCurrentBalance(accountId);

        // Then
        assertTrue(result.isEmpty());
        verify(repository, times(1)).balanceLastMovement(accountId);
    }
}

