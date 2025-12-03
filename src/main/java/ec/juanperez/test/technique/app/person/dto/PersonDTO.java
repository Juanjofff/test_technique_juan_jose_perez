package ec.juanperez.test.technique.app.person.dto;

import ec.juanperez.test.technique.app.person.enums.GenderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PersonDTO implements Serializable {

    private String name;
    private GenderType gender;
    private String identification;
    private String address;
    private String phone;

}
