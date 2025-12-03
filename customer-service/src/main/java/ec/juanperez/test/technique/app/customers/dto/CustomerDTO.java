package ec.juanperez.test.technique.app.customers.dto;

import ec.juanperez.test.technique.app.common.enums.StatusType;
import ec.juanperez.test.technique.app.person.dto.PersonDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomerDTO extends PersonDTO implements Serializable {

    private Long id;
    private String password;
    private StatusType status;
}