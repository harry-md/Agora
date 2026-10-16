package com.agora.manufacturer.persistence;

import com.agora.manufacturer.ManufacturerAdminResponse;
import com.agora.manufacturer.domain.Manufacturer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, UUID> {

    boolean existsBySlug(String slug);

    Optional<Manufacturer> findBySlug(String slug);

    @Query(value = """
        SELECT new com.agora.manufacturer.ManufacturerAdminResponse(
            m.id, m.name, m.slug, m.logoUrl, m.description, m.countryId, c.name, m.active
        )
        FROM Manufacturer m
        JOIN Country c ON c.id = m.countryId
        """, countQuery = """
            SELECT COUNT(m)
            FROM Manufacturer m
            JOIN Country c ON c.id = m.countryId
            """)
    Page<ManufacturerAdminResponse> findAllAdmin(Pageable pageable);
}
