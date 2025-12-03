package ec.juanperez.test.technique.app.accounts.controller;

import ec.juanperez.test.technique.app.accounts.dto.AccountDTO;
import ec.juanperez.test.technique.app.accounts.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "Accounts management API")
public class AccountController {

    private final AccountService service;

    @Operation(summary = "Get all accounts", description = "Retrieve all active accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDTO.class)))
    })
    @GetMapping
    public Mono<ResponseEntity<Flux<AccountDTO>>> findAll() {
        log.info("Finding all accounts");
        return Mono.fromCallable(service::findAll)
                .map(accounts -> ResponseEntity.ok(Flux.fromIterable(accounts)))
                .onErrorResume(e -> {
                    log.error("Error finding all accounts", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Get account by ID", description = "Retrieve a account by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<AccountDTO>> findById(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id) {
        log.info("Finding account with id: {}", id);
        return Mono.fromCallable(() -> this.service.findById(id))
                .map(optional -> optional
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .onErrorResume(e -> {
                    log.error("Error finding account with id: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Create a new account", description = "Create a new account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDTO.class)))
    })
    @PostMapping
    public Mono<ResponseEntity<AccountDTO>> create(@RequestBody Mono<AccountDTO> accountDTOMono) {
        log.info("Creating new account");
        return accountDTOMono
                .flatMap(accountDTO -> Mono.fromCallable(() -> this.service.create(accountDTO)))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error creating account", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Update a account", description = "Update an existing account by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "400", description = "Account is deleted")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<AccountDTO>> update(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id,
            @RequestBody Mono<AccountDTO> accountDTOMono) {
        log.info("Updating account with id: {}", id);
        return accountDTOMono
                .flatMap(accountDTO -> Mono.fromCallable(() -> this.service.update(id, accountDTO)))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error updating account with id: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Delete a account", description = "Delete a account by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id) {
        log.info("Deleting account with id: {}", id);
        return Mono.fromRunnable(() -> this.service.delete(id))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error deleting account with id: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

}
