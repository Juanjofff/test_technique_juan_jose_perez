package ec.juanperez.test.technique.app.movements.model;

import ec.juanperez.test.technique.app.accounts.model.Account;
import ec.juanperez.test.technique.app.movements.enums.MovementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "movimientos")
public class Movements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento")
    private MovementType movementType;

    @Column(name = "valor")
    private BigDecimal value;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    @Column(name = "saldo")
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private Account account;


}
