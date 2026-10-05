package za.co.espaza.backend.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import za.co.espaza.backend.Enum.MovementType;
import za.co.espaza.backend.dto.request.AdjustmentRequest;
import za.co.espaza.backend.dto.request.MovementFilterRequest;
import za.co.espaza.backend.dto.response.StockMovementResponse;
import za.co.espaza.backend.security.CurrentUserService;
import za.co.espaza.backend.service.StockMovementService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movements")
@PreAuthorize("hasRole('ADMIN')")
public class StockMovementController {
    private final StockMovementService stockMovementService;
    private final CurrentUserService currentUserService;

    public StockMovementController(StockMovementService stockMovementService,
                                   CurrentUserService currentUserService) {
        this.stockMovementService = stockMovementService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<StockMovementResponse>>
    getMovements(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) MovementType type
    ) {

        MovementFilterRequest filters = new MovementFilterRequest();

        filters.setFrom(from);
        filters.setTo(to);
        filters.setProductId(productId);
        filters.setType(type);

        return ResponseEntity.ok(stockMovementService.getMovements(filters)
        );
    }

    @PostMapping("/adjustment")
    public ResponseEntity<StockMovementResponse>
    createManualAdjustment(@Valid @RequestBody AdjustmentRequest request) {
        String userId = currentUserService.getCurrentUserId().toString();
        StockMovementResponse response = stockMovementService.createManualAdjustment(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
