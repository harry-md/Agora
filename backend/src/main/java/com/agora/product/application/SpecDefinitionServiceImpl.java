package com.agora.product.application;

import com.agora.exception.ConflictException;
import com.agora.product.CreateSpecDefinitionRequest;
import com.agora.product.SpecDefinitionResponse;
import com.agora.product.SpecDefinitionService;
import com.agora.product.persistence.SpecDefinitionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class SpecDefinitionServiceImpl implements SpecDefinitionService {

    private final SpecDefinitionRepository specDefinitionRepository;
    private final SpecDefinitionMapper specDefinitionMapper;

    @Override
    @Transactional
    public SpecDefinitionResponse createSpecDefinition(CreateSpecDefinitionRequest request) {
        if (specDefinitionRepository.existsByCode(request.code())) {
            throw new ConflictException("Mã thông số đã tồn tại");
        }
        return specDefinitionMapper.toResponse(
                specDefinitionRepository.save(specDefinitionMapper.toEntity(request)));
    }

    @Override
    public boolean existsById(UUID id) {
        return specDefinitionRepository.existsById(id);
    }
}
