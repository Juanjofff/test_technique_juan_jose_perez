package ec.juanperez.test.technique.app.accounts.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import ec.juanperez.test.technique.app.accounts.enums.AccountType;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data  
public class AccountDTO implements Serializable {
    private Long id;
    private String number;
    private AccountType accountType;
    private BigDecimal initialBalance;
    private StatusType status;
    private Long customerId;
    private String customerName;

    public AccountDTO(Long id, String number, AccountType accountType){
        this.id = id;
        this.number = number;
        this.accountType = accountType;
    }
    
}
