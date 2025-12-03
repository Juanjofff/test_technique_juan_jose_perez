package ec.juanperez.test.technique.app.reports.dto;

import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import ec.juanperez.test.technique.app.person.dto.PersonDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReportAccountDTO {

    PersonDTO customer;
    Map<String, List<MovementDTO>> movements;
}
