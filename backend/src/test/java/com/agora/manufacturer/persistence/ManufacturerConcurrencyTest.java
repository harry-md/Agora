package com.agora.manufacturer.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.agora.manufacturer.domain.Manufacturer;

import jakarta.persistence.OptimisticLockException;

import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.DriverManager;
import java.util.UUID;

/**
 * Persistence integration test for Flyway and Hibernate version checks using two overlapping
 * transactions. Uses only a disposable PostgreSQL database whose name begins with agora_test_.
 */
@EnabledIfEnvironmentVariable(named = "AGORA_TEST_DB_URL", matches = ".+")
class ManufacturerConcurrencyTest {

    private static final UUID LEGACY_ID = UUID.randomUUID();
    private static SessionFactory sessions;

    @BeforeAll
    static void migrateAndStartHibernate() throws Exception {
        String url = System.getenv("AGORA_TEST_DB_URL");
        try (var connection = DriverManager.getConnection(url, "postgres", "")) {
            assertTrue(
                    connection.getCatalog().startsWith("agora_test_"),
                    "This test requires a disposable agora_test_ database");
        }
        Flyway.configure().dataSource(url, "postgres", "").target("5").load().migrate();
        try (var connection = DriverManager.getConnection(url, "postgres", "");
                var insert = connection.prepareStatement("""
                    INSERT INTO manufacturers (id, name, slug, logo_url, country_id, is_active)
                    VALUES (?, 'Legacy', 'legacy', 'old-logo', ?, true)
                    """)) {
            insert.setObject(1, LEGACY_ID);
            insert.setObject(2, UUID.randomUUID());
            insert.executeUpdate();
        }
        Flyway.configure().dataSource(url, "postgres", "").load().migrate();
        sessions = new Configuration()
                .addAnnotatedClass(Manufacturer.class)
                .setProperty("hibernate.connection.url", url)
                .setProperty("hibernate.connection.username", "postgres")
                .setProperty("hibernate.connection.password", "")
                .setProperty(
                        "hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl")
                .setProperty("hibernate.hbm2ddl.auto", "validate")
                .buildSessionFactory();
    }

    @AfterAll
    static void closeHibernate() {
        if (sessions != null) {
            sessions.close();
        }
    }

    @Test
    void migrationInitializesExistingRowsAndHibernateInitializesNewRows() {
        try (var session = sessions.openSession()) {
            assertEquals(0L, session.find(Manufacturer.class, LEGACY_ID).getVersion());
            var transaction = session.beginTransaction();
            var manufacturer = newManufacturer();
            session.persist(manufacturer);
            transaction.commit();

            assertNotNull(manufacturer.getId());
            assertEquals(0L, manufacturer.getVersion());
        }
    }

    @Test
    void twoTransactionsReadingSameVersionCannotOverwriteEachOther() {
        UUID id;
        try (var session = sessions.openSession()) {
            var transaction = session.beginTransaction();
            var manufacturer = newManufacturer();
            session.persist(manufacturer);
            transaction.commit();
            id = manufacturer.getId();
        }

        try (var first = sessions.openSession();
                var second = sessions.openSession()) {
            var firstTransaction = first.beginTransaction();
            var secondTransaction = second.beginTransaction();
            var firstCopy = first.find(Manufacturer.class, id);
            var secondCopy = second.find(Manufacturer.class, id);
            assertEquals(firstCopy.getVersion(), secondCopy.getVersion());

            firstCopy.setName("First update");
            firstCopy.setLogoUrl("first-logo");
            firstTransaction.commit();

            secondCopy.setName("Second update");
            secondCopy.setLogoUrl("second-logo");
            var failure = assertThrows(RuntimeException.class, secondTransaction::commit);
            assertTrue(isOptimisticConflict(failure), failure.toString());
            if (secondTransaction.isActive()) {
                secondTransaction.rollback();
            }
        }

        try (var session = sessions.openSession()) {
            var saved = session.find(Manufacturer.class, id);
            assertEquals("First update", saved.getName());
            assertEquals("first-logo", saved.getLogoUrl());
            assertEquals(1L, saved.getVersion());
        }
    }

    private static boolean isOptimisticConflict(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof OptimisticLockException || cause instanceof StaleStateException) {
                return true;
            }
        }
        return false;
    }

    private static Manufacturer newManufacturer() {
        return Manufacturer.builder()
                .name("Manufacturer")
                .slug("manufacturer-" + UUID.randomUUID())
                .logoUrl("old-logo")
                .countryId(UUID.randomUUID())
                .build();
    }
}
