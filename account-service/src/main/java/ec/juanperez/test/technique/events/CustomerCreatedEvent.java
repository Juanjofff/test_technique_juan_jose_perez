package ec.juanperez.test.technique.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent implements Serializable {
    private Long customerId;
    private String name;
    private String identification;
    private String status;
}

