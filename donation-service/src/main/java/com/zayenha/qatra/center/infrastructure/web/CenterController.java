package com.zayenha.qatra.center.infrastructure.web;

import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases;
import com.zayenha.qatra.center.domain.port.in.CenterQueryUseCases;
import com.zayenha.qatra.shared.domain.SearchCriteria;
import com.zayenha.qatra.center.infrastructure.web.dto.request.CreateCenterRequest;
import com.zayenha.qatra.center.infrastructure.web.dto.response.CenterResponse;
import com.zayenha.qatra.center.infrastructure.web.mapper.CenterMapper;
import com.zayenha.qatra.shared.web.ApiResponse;
import com.zayenha.qatra.shared.web.PageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/centers")
@RequiredArgsConstructor
public class CenterController {

    private final CenterCommandUseCases commandUseCases;
    private final CenterQueryUseCases queryUseCases;

    @PostMapping
    public ResponseEntity<ApiResponse<CenterResponse>> create(
            @Valid @RequestBody CreateCenterRequest request) {
        var command = new CenterCommandUseCases.CreateCenterCommand(
            request.name(), request.address(), request.city(),
            request.country(), request.postalCode(), request.phone(),
            request.email(), request.latitude(), request.longitude(),
            request.facilityType(), request.operatingHours(),
            request.totalCapacity(), request.maxRegular(), request.slotPeriod()
        );
        var center = commandUseCases.create(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(CenterMapper.toResponse(center)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CenterResponse>> getById(@PathVariable Long id) {
        var center = queryUseCases.getById(id);
        return ResponseEntity.ok(ApiResponse.success(CenterMapper.toResponse(center)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CenterResponse>>> getAll(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        var criteria = new SearchCriteria(search, sortBy, sortDirection,
            PageHelper.toPageIndex(page), size);
        var result = queryUseCases.getAll(criteria);
        var centers = result.content().stream()
                .map(CenterMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(centers, PageHelper.fromDomain(result)));
    }
}
