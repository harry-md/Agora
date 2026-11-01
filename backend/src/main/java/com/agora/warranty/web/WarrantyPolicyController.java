package com.agora.warranty.web;

import com.agora.warranty.CreateWarrantyPolicyRequest;
import com.agora.warranty.WarrantyPolicyResponse;
import com.agora.warranty.WarrantyPolicyService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/warranty-policies")
public class WarrantyPolicyController {
    private final WarrantyPolicyService warrantyPolicyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarrantyPolicyResponse> create(
            @Valid @RequestBody CreateWarrantyPolicyRequest request) {
        return new ResponseEntity<>(
                this.warrantyPolicyService.createWarrantyPolicy(request), HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<WarrantyPolicyResponse> getCurrent(@PathVariable String code) {
        return ResponseEntity.ok(this.warrantyPolicyService.getCurrentByCode(code));
    }

    @GetMapping("/{code}/versions/{version}")
    public ResponseEntity<WarrantyPolicyResponse> getByVersion(
            @PathVariable String code, @PathVariable int version) {
        return ResponseEntity.ok(this.warrantyPolicyService.getByCodeAndVersion(code, version));
    }

    @GetMapping("/{code}/versions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WarrantyPolicyResponse>> listVersions(@PathVariable String code) {
        return ResponseEntity.ok(this.warrantyPolicyService.getAllByCode(code));
    }
}
