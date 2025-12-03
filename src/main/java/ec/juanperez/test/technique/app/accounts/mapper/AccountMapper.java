package ec.juanperez.test.technique.app.accounts.mapper;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.accounts.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "customerId", source = "customer.id")
    AccountDTO toDto(Account account);

    @Mapping(target = "customer.id", source = "customerId")
    Account toEntity(AccountDTO accountDTO);
}
