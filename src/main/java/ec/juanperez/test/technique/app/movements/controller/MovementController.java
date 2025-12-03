package ec.juanperez.test.technique.app.movements.controller;

import ec.juanperez.test.technique.app.movements.dto.MovementDTO;
import ec.juanperez.test.technique.app.movements.dto.RegisterMovementRequest;
import ec.juanperez.test.technique.app.movements.enums.MovementType;
import ec.juanperez.test.technique.app.movements.service.MovementService;
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

import java.math.BigDecimal;

@CrossOrigin
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movements")
@Tag(name = "Movements", description = "Movements management API")
public class MovementController {

    private final MovementService service;

    @Operation(summary = "Get all movements", description = "Retrieve all active movements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovementDTO.class)))
    })
    @GetMapping
    public Mono<ResponseEntity<Flux<MovementDTO>>> findAll() {
        log.info("Finding all movements");
        return Mono.fromCallable(service::findAll)
                .map(movements -> ResponseEntity.ok(Flux.fromIterable(movements)))
                .onErrorResume(e -> {
                    log.error("Error finding all movements", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Get movement by ID", description = "Retrieve a movement by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "movement found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovementDTO.class))),
            @ApiResponse(responseCode = "404", description = "movement not found")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<MovementDTO>> findById(
            @Parameter(description = "movement ID", required = true) @PathVariable Long id) {
        log.info("Finding movement with id: {}", id);
        return Mono.fromCallable(() -> this.service.findById(id))
                .map(optional -> optional
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .onErrorResume(e -> {
                    log.error("Error finding movement with id: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Create a new movement", description = "Create a new movement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "movement created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovementDTO.class)))
    })
    @PostMapping
    public Mono<ResponseEntity<MovementDTO>> create(@RequestBody Mono<MovementDTO> MovementDTOMono) {
        log.info("Creating new movement");
        return MovementDTOMono
                .flatMap(MovementDTO -> Mono.fromCallable(() -> this.service.create(MovementDTO)))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error creating movement", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Update a movement", description = "Update an existing movement by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "movement updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovementDTO.class))),
            @ApiResponse(responseCode = "404", description = "movement not found"),
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<MovementDTO>> update(
            @Parameter(description = "movement ID", required = true) @PathVariable Long id,
            @RequestBody Mono<MovementDTO> MovementDTOMono) {
        log.info("Updating movement with id: {}", id);
        return MovementDTOMono
                .flatMap(MovementDTO -> Mono.fromCallable(() -> this.service.update(id, MovementDTO)))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error updating movement with id: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Delete a movement", description = "Delete a movement by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "movement deleted successfully"),
            @ApiResponse(responseCode = "404", description = "movement not found")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(
            @Parameter(description = "movement ID", required = true) @PathVariable Long id) {
        log.info("Deleting movement with id: {}", id);
        return Mono.fromRunnable(() -> this.service.delete(id))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error deleting movement with id: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Operation(summary = "Register a movement type credit", description = "Register a movement type credit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "movement registered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovementDTO.class)))
    })
    @PostMapping("/register/credit")
    public Mono<ResponseEntity<MovementDTO>> registerCredit(@RequestBody Mono<RegisterMovementRequest> requestMono) {
        return requestMono
                .doOnNext(request -> log.info("Registering credit movement in account: {} with value: {}", request.getAccountId(), request.getValue()))
                .flatMap(request -> Mono.fromCallable(() -> this.service.registerMovementByType(request.getAccountId(), MovementType.CREDIT, request.getValue()))
                        .onErrorMap(e -> e.getCause() != null ? e.getCause() : e)
                        .map(ResponseEntity::ok));
    }

    @Operation(summary = "Register a movement type debit", description = "Register a movement type debit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "movement registered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovementDTO.class)))
    })
    @PostMapping("/register/debit")
    public Mono<ResponseEntity<MovementDTO>> registerDebit(@RequestBody Mono<RegisterMovementRequest> requestMono) {
        return requestMono
                .doOnNext(request -> log.info("Registering debit movement in account: {} with value: {}", request.getAccountId(), request.getValue()))
                .flatMap(request -> Mono.fromCallable(() -> this.service.registerMovementByType(request.getAccountId(), MovementType.DEBIT, request.getValue()))
                        .onErrorMap(e -> e.getCause() != null ? e.getCause() : e)
                        .map(ResponseEntity::ok));
    }

}
