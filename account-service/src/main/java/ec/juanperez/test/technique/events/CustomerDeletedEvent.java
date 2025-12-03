package ec.juanperez.test.technique.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeletedEvent implements Serializable {
    private Long customerId;
}

