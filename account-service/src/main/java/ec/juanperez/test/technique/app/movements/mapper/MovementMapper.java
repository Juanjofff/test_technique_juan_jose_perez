package ec.juanperez.test.technique.app.movements.mapper;

import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import ec.juanperez.test.technique.app.movements.model.Movements;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MovementMapper{

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountNumber", source = "account.number")
    MovementDTO toDTO(Movements movements);

    @Mapping(target = "account.id", source = "accountId")
    Movements toEntity(MovementDTO movementDTO);
}
