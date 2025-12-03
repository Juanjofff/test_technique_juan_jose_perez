package ec.juanperez.test.technique.app.customers.mapper;

import org.mapstruct.Mapper;
import ec.juanperez.test.technique.app.customers.dto.CustomerDTO;
import ec.juanperez.test.technique.app.customers.model.Customer;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {
    CustomerDTO toDTO(Customer client);
    Customer toEntity(CustomerDTO clientDTO);
}
