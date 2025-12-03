package ec.juanperez.test.technique.model;

import ec.juanperez.test.technique.app.common.enums.StatusType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CUSTOMER_REFERENCE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerReference {

    @Id
    @Column(name = "id_cliente")
    private Long id;

    @Column(name = "nombre", length = 300, nullable = false)
    private String name;

    @Column(name = "identificacion", length = 50, nullable = false)
    private String identification;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 50, nullable = false)
    private StatusType status;
}

