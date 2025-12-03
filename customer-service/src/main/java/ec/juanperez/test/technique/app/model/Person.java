package ec.juanperez.test.technique.app.person.model;

import ec.juanperez.test.technique.app.person.enums.GenderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Column(name = "nombre", length = 300, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 50, nullable = false)
    private GenderType gender;

    @Column(name = "identificacion", length = 50, nullable = false)
    private String identification;
    
    @Column(name = "direccion", length = 300, nullable = false)
    private String address;

    @Column(name = "telefono", length = 20, nullable = false)
    private String phone;

}
