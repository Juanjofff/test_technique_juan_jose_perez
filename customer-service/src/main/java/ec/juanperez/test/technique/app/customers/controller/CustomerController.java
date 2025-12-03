package ec.juanperez.test.technique.app.customers.controller;

import ec.juanperez.test.technique.app.customers.dto.CustomerDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import ec.juanperez.test.technique.app.customers.service.CustomerService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("/customers")
@Tag(name = "Customers", description = "Customer management API")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Get all customers", description = "Retrieve all active customers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDTO.class)))
    })
    @GetMapping
    public Mono<ResponseEntity<Flux<CustomerDTO>>> findAll() {
        log.info("Finding all customers");
        return Mono.fromCallable(customerService::findAll)
                .map(customers -> ResponseEntity.ok(Flux.fromIterable(customers)))
                .onErrorResume(e -> {
                    log.error("Error finding all customers", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Get customer by ID", description = "Retrieve a customer by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CustomerDTO>> findById(
            @Parameter(description = "Customer ID", required = true) @PathVariable Long id) {
        log.info("Finding customer with id: {}", id);
        return Mono.fromCallable(() -> customerService.findById(id))
                .map(optional -> optional
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .onErrorResume(e -> {
                    log.error("Error finding customer with id: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Create a new customer", description = "Create a new customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDTO.class)))
    })
    @PostMapping
    public Mono<ResponseEntity<CustomerDTO>> create(@RequestBody Mono<CustomerDTO> customerDTOMono) {
        log.info("Creating new customer");
        return customerDTOMono
                .flatMap(customerDTO -> Mono.fromCallable(() -> customerService.create(customerDTO)))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error creating customer", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Update a customer", description = "Update an existing customer by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Customer is deleted")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CustomerDTO>> update(
            @Parameter(description = "Customer ID", required = true) @PathVariable Long id,
            @RequestBody Mono<CustomerDTO> customerDTOMono) {
        log.info("Updating customer with id: {}", id);
        return customerDTOMono
                .flatMap(customerDTO -> Mono.fromCallable(() -> customerService.update(id, customerDTO)))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error updating customer with id: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Delete a customer", description = "Delete a customer by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(
            @Parameter(description = "Customer ID", required = true) @PathVariable Long id) {
        log.info("Deleting customer with id: {}", id);
        return Mono.fromRunnable(() -> customerService.delete(id))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error deleting customer with id: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}
