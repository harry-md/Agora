package com.agora.manufacturer.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.agora.exception.GlobalExceptionHandler;
import com.agora.exception.ResourceNotFoundException;
import com.agora.manufacturer.ManufacturerAdminResponse;
import com.agora.manufacturer.ManufacturerResponse;
import com.agora.manufacturer.ManufacturerService;
import com.agora.security.TokenService;
import com.agora.security.internal.SecurityConfig;

import jakarta.persistence.OptimisticLockException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

@WebMvcTest(controllers = ManufacturerController.class, properties = "spring.config.import=")
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ManufacturerControllerTest {

    private static final String BASE = "/api/manufacturers";
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID COUNTRY_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ManufacturerService manufacturers;

    @MockitoBean
    private TokenService tokens;

    @Test
    void anonymousCanReadDetail() throws Exception {
        when(manufacturers.getBySlug("apple")).thenReturn(response());

        mvc.perform(get(BASE + "/apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.countryId").value(COUNTRY_ID.toString()))
                .andExpect(jsonPath("$.active").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUSTOMER", "STAFF", "ADMIN"})
    void authenticatedUsersCanReadDetail(String role) throws Exception {
        when(manufacturers.getBySlug("apple")).thenReturn(response());
        mvc.perform(get(BASE + "/apple").with(user("viewer").roles(role)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "create", "update"})
    void anonymousCannotUseAdminOperations(String operation) throws Exception {
        mvc.perform(adminRequest(operation)).andExpect(status().isUnauthorized());
        verifyNoInteractions(manufacturers);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUSTOMER", "STAFF"})
    void nonAdminCannotListCreateOrUpdate(String role) throws Exception {
        for (String operation : List.of("list", "create", "update")) {
            mvc.perform(adminRequest(operation).with(user("viewer").roles(role)))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(manufacturers);
    }

    @Test
    void adminCanListWithPagination() throws Exception {
        var pageable = PageRequest.of(1, 5, Sort.by("name"));
        when(manufacturers.getAllForAdmin(any()))
                .thenReturn(new PageImpl<>(List.of(adminResponse()), pageable, 6));

        mvc.perform(get(BASE + "/admin")
                        .param("page", "1")
                        .param("size", "5")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.content[0].countryId").value(COUNTRY_ID.toString()))
                .andExpect(jsonPath("$.content[0].countryName").value("Việt Nam"));
        verify(manufacturers).getAllForAdmin(pageable);
    }

    @Test
    void adminCanCreateWithMultipartLogo() throws Exception {
        when(manufacturers.create(any())).thenReturn(response());

        mvc.perform(createRequest().with(user("admin").roles("ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("apple"));
        verify(manufacturers)
                .create(argThat(request -> request.name().equals("Apple")
                        && request.countryId().equals(COUNTRY_ID)
                        && !request.logo().isEmpty()));
    }

    @Test
    void adminCanUpdateWithoutReplacingLogo() throws Exception {
        when(manufacturers.update(eq(ID), any())).thenReturn(adminResponse());

        mvc.perform(updateRequest().with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        verify(manufacturers)
                .update(
                        eq(ID),
                        argThat(request -> request.name().equals("Apple")
                                && request.logo() == null
                                && request.active()));
    }

    @Test
    void adminCanReplaceLogo() throws Exception {
        when(manufacturers.update(eq(ID), any())).thenReturn(adminResponse());

        mvc.perform(updateRequest().file(logo()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        verify(manufacturers).update(eq(ID), argThat(request -> !request.logo().isEmpty()));
    }

    @Test
    void createRequiresLogoAndCountry() throws Exception {
        mvc.perform(multipart(BASE).param("name", "Apple").with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart(BASE)
                        .file(logo())
                        .param("name", "Apple")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(manufacturers);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-name-that-is-too-long"})
    void createValidatesName(String name) throws Exception {
        String invalidName = name.isBlank() ? name : name.repeat(10);
        mvc.perform(multipart(BASE)
                        .file(logo())
                        .param("name", invalidName)
                        .param("countryId", COUNTRY_ID.toString())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(manufacturers);
    }

    @Test
    void updateRequiresNameCountryAndActive() throws Exception {
        mvc.perform(multipart(HttpMethod.PUT, BASE + "/" + ID)
                        .param("name", "Apple")
                        .param("countryId", COUNTRY_ID.toString())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart(HttpMethod.PUT, BASE + "/" + ID)
                        .param("active", "true")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(manufacturers);
    }

    @Test
    void invalidCountryIdReturns400() throws Exception {
        mvc.perform(multipart(BASE)
                        .file(logo())
                        .param("name", "Apple")
                        .param("countryId", "invalid")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(manufacturers);
    }

    @Test
    void missingDetailReturns404() throws Exception {
        when(manufacturers.getBySlug("missing"))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy nhà sản xuất"));
        mvc.perform(get(BASE + "/missing")).andExpect(status().isNotFound());
    }

    @Test
    void optimisticConflictReturns409WithRetryMessageWithoutLeakingDetails() throws Exception {
        for (RuntimeException failure : List.of(
                new ObjectOptimisticLockingFailureException("private database details", null),
                new OptimisticLockException("private database details"))) {
            when(manufacturers.update(eq(ID), any())).thenThrow(failure);

            mvc.perform(updateRequest().with(user("admin").roles("ADMIN")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message")
                            .value("Dữ liệu vừa được cập nhật bởi yêu cầu khác. Vui lòng"
                                    + " tải lại và thử lại."));
        }
    }

    private AbstractMockHttpServletRequestBuilder<?> adminRequest(String operation) {
        return switch (operation) {
            case "list" -> get(BASE + "/admin");
            case "create" -> createRequest();
            case "update" -> updateRequest();
            default -> throw new IllegalArgumentException(operation);
        };
    }

    private MockMultipartHttpServletRequestBuilder createRequest() {
        var request = multipart(BASE).file(logo());
        request.param("name", "Apple").param("countryId", COUNTRY_ID.toString());
        return request;
    }

    private MockMultipartHttpServletRequestBuilder updateRequest() {
        var request = multipart(HttpMethod.PUT, BASE + "/" + ID);
        request.param("name", "Apple")
                .param("countryId", COUNTRY_ID.toString())
                .param("active", "true");
        return request;
    }

    private MockMultipartFile logo() {
        return new MockMultipartFile("logo", "logo.png", "image/png", new byte[] {1});
    }

    private ManufacturerResponse response() {
        return new ManufacturerResponse(ID, "Apple", "apple", "logo-url", null, COUNTRY_ID);
    }

    private ManufacturerAdminResponse adminResponse() {
        return new ManufacturerAdminResponse(
                ID, "Apple", "apple", "logo-url", null, COUNTRY_ID, "Việt Nam", true);
    }
}
