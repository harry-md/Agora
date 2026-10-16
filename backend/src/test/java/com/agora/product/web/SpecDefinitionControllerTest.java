package com.agora.product.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.agora.category.CategoryService;
import com.agora.category.CategorySpecResponse;
import com.agora.category.CategorySpecService;
import com.agora.category.CreateCategorySpecRequest;
import com.agora.category.web.CategoryController;
import com.agora.exception.ConflictException;
import com.agora.exception.GlobalExceptionHandler;
import com.agora.exception.ResourceNotFoundException;
import com.agora.product.CreateSpecDefinitionRequest;
import com.agora.product.SpecDataType;
import com.agora.product.SpecDefinitionResponse;
import com.agora.product.SpecDefinitionService;
import com.agora.security.TokenService;
import com.agora.security.internal.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

@WebMvcTest(
        controllers = {SpecDefinitionController.class, CategoryController.class},
        properties = "spring.config.import=")
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SpecDefinitionControllerTest {

    private static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SPEC_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String DEFINITIONS = "/api/spec-definitions";
    private static final String ASSIGNMENTS =
            "/api/categories/00000000-0000-0000-0000-000000000001/filters";
    private static final String DEFINITION_BODY = """
        {
          "code": "ram",
          "name": "RAM",
          "dataType": "NUMBER",
          "unit": "GB"
        }
        """;
    private static final String ASSIGNMENT_BODY = """
        {
          "specDefinitionId": "00000000-0000-0000-0000-000000000002",
          "required": true,
          "filterable": true,
          "displayOrder": 1
        }
        """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SpecDefinitionService definitions;

    @MockitoBean
    private CategorySpecService assignments;

    @MockitoBean
    private CategoryService categories;

    @MockitoBean
    private TokenService tokens;

    @ParameterizedTest
    @ValueSource(strings = {DEFINITIONS, ASSIGNMENTS})
    void anonymousCannotCreate(String path) throws Exception {
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(definitions, assignments);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUSTOMER", "STAFF"})
    void nonAdminCannotCreateEitherResource(String role) throws Exception {
        mvc.perform(post(DEFINITIONS)
                        .with(user("user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEFINITION_BODY))
                .andExpect(status().isForbidden());
        mvc.perform(post(ASSIGNMENTS)
                        .with(user("user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASSIGNMENT_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(definitions, assignments);
    }

    @Test
    void adminCanCreateDefinition() throws Exception {
        when(definitions.createSpecDefinition(any()))
                .thenReturn(new SpecDefinitionResponse(
                        SPEC_ID, "ram", "RAM", SpecDataType.NUMBER, "GB"));

        mvc.perform(post(DEFINITIONS)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEFINITION_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SPEC_ID.toString()))
                .andExpect(jsonPath("$.dataType").value("NUMBER"));
        verify(definitions)
                .createSpecDefinition(
                        new CreateSpecDefinitionRequest("ram", "RAM", SpecDataType.NUMBER, "GB"));
    }

    @Test
    void adminCanAssignDefinitionToCategory() throws Exception {
        when(assignments.create(eq(CATEGORY_ID), any()))
                .thenReturn(new CategorySpecResponse(SPEC_ID, true, true, 1));

        mvc.perform(post(ASSIGNMENTS)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASSIGNMENT_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filterable").value(true));
        verify(assignments)
                .create(CATEGORY_ID, new CreateCategorySpecRequest(SPEC_ID, true, true, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", """
        {
          "code": " ",
          "name": "RAM",
          "dataType": "NUMBER"
        }
        """, """
                {
                  "code": "ram",
                  "name": "RAM",
                  "dataType": "OPTION"
                }
                """, "{"})
    void invalidDefinitionReturns400(String body) throws Exception {
        mvc.perform(post(DEFINITIONS)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(definitions);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", """
        {
          "specDefinitionId": "invalid"
        }
        """, """
                {
                  "specDefinitionId": "00000000-0000-0000-0000-000000000002",
                  "displayOrder": -1
                }
                """})
    void invalidAssignmentReturns400(String body) throws Exception {
        mvc.perform(post(ASSIGNMENTS)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(assignments);
    }

    @Test
    void missingReferenceReturns404() throws Exception {
        when(assignments.create(eq(CATEGORY_ID), any()))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy định nghĩa thông số"));
        mvc.perform(post(ASSIGNMENTS)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASSIGNMENT_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateDefinitionReturns409() throws Exception {
        when(definitions.createSpecDefinition(any()))
                .thenThrow(new ConflictException("Mã thông số đã tồn tại"));
        mvc.perform(post(DEFINITIONS)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEFINITION_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void databaseConstraintRaceReturns409WithoutLeakingSql() throws Exception {
        when(assignments.create(eq(CATEGORY_ID), any()))
                .thenThrow(new DataIntegrityViolationException("private database details"));
        mvc.perform(post(ASSIGNMENTS)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASSIGNMENT_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(content().string(not(containsString("private database details"))));
    }
}
