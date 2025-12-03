package ec.juanperez.test.technique.app.reports.dto;

import ec.juanperez.test.technique.app.dto.CustomerDTO;
import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReportAccountDTO {

    CustomerDTO customer;
    Map<String, List<MovementDTO>> movements;
}
