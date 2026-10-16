package com.agora.manufacturer.web;

import com.agora.manufacturer.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/manufacturers")
@RequiredArgsConstructor
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ManufacturerAdminResponse>> listAdmin(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(manufacturerService.getAllForAdmin(pageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ManufacturerResponse> getManufacturerBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(manufacturerService.getBySlug(slug));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManufacturerResponse> create(
            @Valid @ModelAttribute CreateManufacturerRequest request) {
        return new ResponseEntity<>(manufacturerService.create(request), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManufacturerAdminResponse> update(
            @PathVariable UUID id, @Valid @ModelAttribute UpdateManufacturerRequest request) {
        return ResponseEntity.ok(manufacturerService.update(id, request));
    }
}
