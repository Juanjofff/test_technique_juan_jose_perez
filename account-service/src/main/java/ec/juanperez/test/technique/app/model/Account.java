package ec.juanperez.test.technique.app.accounts.model;

import java.math.BigDecimal;

import ec.juanperez.test.technique.app.accounts.enums.AccountType;
import ec.juanperez.test.technique.app.common.enums.StatusType;
import ec.juanperez.test.technique.app.customers.model.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CUENTA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Long id;

    @Column(name = "numero_cuenta", length = 50)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", length = 50, nullable = false)
    private AccountType accountType;

    @Column(name = "saldo_inicial", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal initialBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private StatusType status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Customer customer;
}
