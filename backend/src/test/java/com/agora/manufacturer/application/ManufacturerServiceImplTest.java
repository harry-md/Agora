package com.agora.manufacturer.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.agora.address.CountryService;
import com.agora.exception.BadRequestException;
import com.agora.exception.ResourceNotFoundException;
import com.agora.manufacturer.CreateManufacturerRequest;
import com.agora.manufacturer.ManufacturerService;
import com.agora.manufacturer.UpdateManufacturerRequest;
import com.agora.manufacturer.domain.Manufacturer;
import com.agora.manufacturer.persistence.ManufacturerRepository;
import com.agora.media.ImageStorageService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@DisplayName("ManufacturerServiceImpl Unit Tests")
class ManufacturerServiceImplTest {

    private final ManufacturerRepository repository = mock(ManufacturerRepository.class);
    private final ImageStorageService images = mock(ImageStorageService.class);
    private final CountryService countries = mock(CountryService.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final ManufacturerServiceImpl service = new ManufacturerServiceImpl(
            repository,
            Mappers.getMapper(ManufacturerMapper.class),
            images,
            countries,
            new TransactionTemplate(transactions));
    private final UUID id = UUID.randomUUID();
    private final UUID countryId = UUID.randomUUID();
    private final MockMultipartFile logo =
            new MockMultipartFile("logo", "logo.png", "image/png", new byte[] {1});

    void setUp() {
        when(transactions.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        when(countries.existsById(countryId)).thenReturn(true);
        when(countries.getNameById(countryId)).thenReturn("Việt Nam");
    }

    @Test
    void createNormalizesVietnameseNameAndDefaultsToActive() {
        when(images.upload(logo)).thenReturn("new-logo");
        when(repository.save(any())).thenAnswer(invocation -> {
            Manufacturer manufacturer = invocation.getArgument(0);
            manufacturer.setId(id);
            assertTrue(manufacturer.isActive());
            return manufacturer;
        });

        var response = service.create(
                new CreateManufacturerRequest("  Điện Tử Việt  ", logo, "Description", countryId));

        assertEquals(id, response.id());
        assertEquals("Điện Tử Việt", response.name());
        assertEquals("dien-tu-viet", response.slug());
        assertEquals("new-logo", response.logoUrl());
        assertEquals(countryId, response.countryId());
        var order = inOrder(images, transactions, repository);
        order.verify(images).upload(logo);
        order.verify(transactions).getTransaction(any());
        order.verify(repository).save(any());
        order.verify(transactions).commit(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "Apple"})
    void avoidsReservedAndExistingSlugs(String name) {
        when(repository.existsBySlug("apple")).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreateManufacturerRequest(name, logo, null, countryId));

        assertTrue(response.slug().matches(name.toLowerCase() + "-(\\d{4}|10000)"));
    }

    @Test
    void boundsLongSlugIncludingCollisionSuffix() {
        when(repository.existsBySlug("a".repeat(150))).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(
                new CreateManufacturerRequest("a".repeat(200), logo, null, countryId));

        assertEquals(150, response.slug().length());
    }

    @Test
    void rejectsUnknownCountryBeforeUploadingOrWriting() {
        when(countries.existsById(countryId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(createRequest()));

        verifyNoInteractions(images, repository, transactions);
    }

    @Test
    void rejectsEmptyLogoAndUnusableSlugBeforeUploading() {
        var emptyLogo = new MockMultipartFile("logo", new byte[0]);
        assertThrows(
                BadRequestException.class,
                () -> service.create(
                        new CreateManufacturerRequest("Apple", emptyLogo, null, countryId)));
        assertThrows(
                BadRequestException.class,
                () -> service.create(new CreateManufacturerRequest("!!!", logo, null, countryId)));
        verifyNoInteractions(images, repository, transactions);
    }

    @Test
    void cleansUploadedLogoWhenCommitFails() throws IOException {
        when(images.upload(logo)).thenReturn("new-logo");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var failure = new DataIntegrityViolationException("duplicate slug");
        doThrow(failure).when(transactions).commit(any());

        assertSame(
                failure,
                assertThrows(
                        DataIntegrityViolationException.class,
                        () -> service.create(createRequest())));
        verify(images).delete("new-logo");
    }

    @Test
    void updateWithoutLogoPreservesUrlAndSlugWhileUpdatingOtherFields() {
        Manufacturer manufacturer = existingManufacturer();

        var response = service.update(
                id, new UpdateManufacturerRequest("New name", null, null, countryId, false));

        assertEquals("New name", response.name());
        assertEquals("Việt Nam", response.countryName());
        assertEquals("apple", response.slug());
        assertEquals("old-logo", response.logoUrl());
        assertNull(response.description());
        assertFalse(response.active());
        assertFalse(manufacturer.isActive());
        verifyNoInteractions(images);
        verify(transactions).commit(any());
    }

    @Test
    void replacementDeletesOldLogoOnlyAfterCommit() throws IOException {
        existingManufacturer();
        when(images.upload(logo)).thenReturn("new-logo");

        var response = service.update(id, updateRequest());

        assertEquals("new-logo", response.logoUrl());
        var order = inOrder(images, transactions, repository);
        order.verify(images).upload(logo);
        order.verify(transactions).getTransaction(any());
        order.verify(repository).findById(id);
        order.verify(transactions).commit(any());
        order.verify(images).delete("old-logo");
    }

    @Test
    void failedUpdateCleansNewLogoAndPreservesOldFile() throws IOException {
        existingManufacturer();
        when(images.upload(logo)).thenReturn("new-logo");
        doThrow(new DataIntegrityViolationException("commit failed"))
                .when(transactions)
                .commit(any());

        assertThrows(
                DataIntegrityViolationException.class, () -> service.update(id, updateRequest()));

        verify(images).delete("new-logo");
        verify(images, never()).delete("old-logo");
    }

    @Test
    void oldLogoCleanupFailureDoesNotFailCommittedUpdate() throws IOException {
        existingManufacturer();
        when(images.upload(logo)).thenReturn("new-logo");
        doThrow(new IllegalStateException("storage unavailable")).when(images).delete("old-logo");

        assertEquals("new-logo", service.update(id, updateRequest()).logoUrl());
        verify(images, never()).delete("new-logo");
    }

    @Test
    void rejectsMissingManufacturerBeforeUploading() {
        assertThrows(ResourceNotFoundException.class, () -> service.update(id, updateRequest()));
        verifyNoInteractions(images, countries, transactions);
    }

    @Test
    void detailReturns404ForUnknownSlug() {
        assertThrows(ResourceNotFoundException.class, () -> service.getBySlug("missing"));
    }

    @Test
    void cleanupIOExceptionIsSuppressedWithoutMaskingCommitFailure() throws IOException {
        when(images.upload(logo)).thenReturn("new-logo");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var failure = new DataIntegrityViolationException("commit failed");
        var cleanupFailure = new IOException("storage unavailable");
        doThrow(failure).when(transactions).commit(any());
        doThrow(cleanupFailure).when(images).delete("new-logo");

        var thrown = assertThrows(
                DataIntegrityViolationException.class, () -> service.create(createRequest()));

        assertSame(failure, thrown);
        assertArrayEquals(new Throwable[] {cleanupFailure}, thrown.getSuppressed());
    }

    @Test
    void updateEvictsOnlyItsSlugAndAllListPagesEvenWhenOldLogoDeletionFails() throws IOException {
        existingManufacturer();
        when(images.upload(logo)).thenReturn("new-logo");
        doThrow(new IOException("storage unavailable")).when(images).delete("old-logo");

        var caches = new ConcurrentMapCacheManager("manufacturer", "admin-manufacturers");
        var detail = caches.getCache("manufacturer");
        var lists = caches.getCache("admin-manufacturers");
        detail.put("apple", "stale apple");
        detail.put("samsung", "unchanged samsung");
        lists.put("page-0", "stale list");
        lists.put("page-1", "stale list");

        var response = cachedService(caches).update(id, updateRequest());

        assertEquals("new-logo", response.logoUrl());
        assertNull(detail.get("apple"));
        assertEquals("unchanged samsung", detail.get("samsung", String.class));
        assertNull(lists.get("page-0"));
        assertNull(lists.get("page-1"));
        verify(images, never()).delete("new-logo");
    }

    @Test
    void failedUpdateKeepsExistingCacheEntries() {
        existingManufacturer();
        doThrow(new DataIntegrityViolationException("commit failed"))
                .when(transactions)
                .commit(any());
        var caches = new ConcurrentMapCacheManager("manufacturer", "admin-manufacturers");
        caches.getCache("manufacturer").put("apple", "current apple");
        caches.getCache("admin-manufacturers").put("page-0", "current list");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> cachedService(caches)
                        .update(
                                id,
                                new UpdateManufacturerRequest(
                                        "New name", null, null, countryId, true)));

        assertEquals("current apple", caches.getCache("manufacturer").get("apple", String.class));
        assertEquals(
                "current list", caches.getCache("admin-manufacturers").get("page-0", String.class));
    }

    private ManufacturerService cachedService(ConcurrentMapCacheManager caches) {
        var interceptor = new CacheInterceptor();
        interceptor.setCacheManager(caches);
        interceptor.setCacheOperationSources(new AnnotationCacheOperationSource());
        interceptor.afterPropertiesSet();
        interceptor.afterSingletonsInstantiated();
        var proxy = new ProxyFactory(service);
        proxy.addAdvice(interceptor);
        return (ManufacturerService) proxy.getProxy();
    }

    @Test
    void optimisticConflictCleansNewLogoPreservesOldFileAndCache() throws IOException {
        existingManufacturer();
        when(images.upload(logo)).thenReturn("new-logo");
        var failure = new ObjectOptimisticLockingFailureException(Manufacturer.class, id);
        doThrow(failure).when(transactions).commit(any());
        var caches = new ConcurrentMapCacheManager("manufacturer", "admin-manufacturers");
        caches.getCache("manufacturer").put("apple", "current apple");
        caches.getCache("admin-manufacturers").put("page-0", "current list");

        assertSame(
                failure,
                assertThrows(
                        ObjectOptimisticLockingFailureException.class,
                        () -> cachedService(caches).update(id, updateRequest())));

        verify(images).delete("new-logo");
        verify(images, never()).delete("old-logo");
        assertEquals("current apple", caches.getCache("manufacturer").get("apple", String.class));
        assertEquals(
                "current list", caches.getCache("admin-manufacturers").get("page-0", String.class));
    }

    private CreateManufacturerRequest createRequest() {
        return new CreateManufacturerRequest("Apple", logo, null, countryId);
    }

    private UpdateManufacturerRequest updateRequest() {
        return new UpdateManufacturerRequest("Apple Inc.", logo, "Updated", countryId, true);
    }

    private Manufacturer existingManufacturer() {
        Manufacturer manufacturer = Manufacturer.builder()
                .id(id)
                .name("Apple")
                .slug("apple")
                .logoUrl("old-logo")
                .description("Old description")
                .countryId(countryId)
                .build();
        when(repository.existsById(id)).thenReturn(true);
        when(repository.findById(id)).thenReturn(Optional.of(manufacturer));
        return manufacturer;
    }
}
