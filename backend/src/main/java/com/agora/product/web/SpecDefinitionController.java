package com.agora.product.web;

import com.agora.product.CreateSpecDefinitionRequest;
import com.agora.product.SpecDefinitionResponse;
import com.agora.product.SpecDefinitionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spec-definitions")
@RequiredArgsConstructor
public class SpecDefinitionController {

    private final SpecDefinitionService specDefinitionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecDefinitionResponse> create(
            @Valid @RequestBody CreateSpecDefinitionRequest request) {
        return new ResponseEntity<>(
                specDefinitionService.createSpecDefinition(request), HttpStatus.CREATED);
    }
}
