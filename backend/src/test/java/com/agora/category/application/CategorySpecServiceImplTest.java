package com.agora.category.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.agora.category.CreateCategorySpecRequest;
import com.agora.category.domain.Category;
import com.agora.category.domain.CategorySpec;
import com.agora.category.persistence.CategoryRepository;
import com.agora.category.persistence.CategorySpecRepository;
import com.agora.exception.ConflictException;
import com.agora.exception.ResourceNotFoundException;
import com.agora.product.SpecDefinitionService;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

class CategorySpecServiceImplTest {

    private final CategorySpecRepository repository = mock(CategorySpecRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final SpecDefinitionService definitions = mock(SpecDefinitionService.class);
    private final CategorySpecServiceImpl service = new CategorySpecServiceImpl(
            repository, Mappers.getMapper(CategorySpecMapper.class), categories, definitions);
    private final UUID categoryId = UUID.randomUUID();
    private final UUID definitionId = UUID.randomUUID();
    private final CreateCategorySpecRequest request =
            new CreateCategorySpecRequest(definitionId, false, true, 2);

    @Test
    void createsAssignmentPreservingExplicitFlagsAndOrder() {
        Category category = Category.builder().id(categoryId).build();
        when(categories.existsById(categoryId)).thenReturn(true);
        when(categories.getReferenceById(categoryId)).thenReturn(category);
        when(definitions.existsById(definitionId)).thenReturn(true);
        UUID id = UUID.randomUUID();
        when(repository.save(any(CategorySpec.class))).thenAnswer(invocation -> {
            CategorySpec entity = invocation.getArgument(0);
            entity.setId(id);
            return entity;
        });

        var result = service.create(categoryId, request);

        ArgumentCaptor<CategorySpec> saved = ArgumentCaptor.forClass(CategorySpec.class);
        verify(repository).save(saved.capture());
        assertSame(category, saved.getValue().getCategory());
        assertEquals(definitionId, saved.getValue().getSpecDefinitionId());
        assertEquals(id, result.id());
        assertFalse(result.required());
        assertTrue(result.filterable());
        assertEquals(2, result.displayOrder());
    }

    @Test
    void rejectsMissingCategory() {
        assertThrows(ResourceNotFoundException.class, () -> service.create(categoryId, request));
        verifyNoInteractions(definitions, repository);
    }

    @Test
    void rejectsMissingDefinitionWithoutWriting() {
        when(categories.existsById(categoryId)).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> service.create(categoryId, request));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsDuplicateAssignmentWithoutWriting() {
        when(categories.existsById(categoryId)).thenReturn(true);
        when(definitions.existsById(definitionId)).thenReturn(true);
        when(repository.existsByCategoryIdAndSpecDefinitionId(categoryId, definitionId))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(categoryId, request));
        verify(repository, never()).save(any());
    }
}
