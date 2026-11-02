package com.agora.warranty.persistence;

import com.agora.warranty.domain.WarrantyPolicy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarrantyPolicyRepository extends JpaRepository<WarrantyPolicy, UUID> {
    @Query("""
        SELECT MAX(w.version)
        FROM WarrantyPolicy w
        WHERE w.code = :code
        """)
    Optional<Integer> findMaxVersionByCode(String code);

    Optional<WarrantyPolicy> findFirstByCodeAndEffectiveFromLessThanEqualOrderByVersionDesc(
            String code, Instant now);

    Optional<WarrantyPolicy> findByCodeAndVersion(String code, int version);

    List<WarrantyPolicy> findAllByCodeOrderByVersionDesc(String code);

    boolean existsByDocumentKey(String documentKey);
}
