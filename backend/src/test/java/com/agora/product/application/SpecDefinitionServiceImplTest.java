package com.agora.product.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.agora.exception.ConflictException;
import com.agora.product.CreateSpecDefinitionRequest;
import com.agora.product.SpecDataType;
import com.agora.product.SpecDefinitionResponse;
import com.agora.product.domain.SpecDefinition;
import com.agora.product.persistence.SpecDefinitionRepository;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

class SpecDefinitionServiceImplTest {

    private final SpecDefinitionRepository repository = mock(SpecDefinitionRepository.class);
    private final SpecDefinitionServiceImpl service = new SpecDefinitionServiceImpl(
            repository, Mappers.getMapper(SpecDefinitionMapper.class));

    @Test
    void createsDefinitionWithNormalizedInputAndGeneratedId() {
        UUID id = UUID.randomUUID();
        when(repository.save(any(SpecDefinition.class))).thenAnswer(invocation -> {
            SpecDefinition entity = invocation.getArgument(0);
            entity.setId(id);
            return entity;
        });

        SpecDefinitionResponse result = service.createSpecDefinition(
                new CreateSpecDefinitionRequest(" ram ", " RAM ", SpecDataType.NUMBER, " GB "));

        assertEquals(
                new SpecDefinitionResponse(id, "ram", "RAM", SpecDataType.NUMBER, "GB"), result);
        verify(repository).existsByCode("ram");
    }

    @Test
    void rejectsDuplicateCodeBeforeWriting() {
        when(repository.existsByCode("ram")).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.createSpecDefinition(
                        new CreateSpecDefinitionRequest("ram", "RAM", SpecDataType.NUMBER, "GB")));

        verify(repository, never()).save(any());
    }
}
