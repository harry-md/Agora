package com.agora.address.persistence;

import com.agora.address.domain.Address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    @Query("""
        SELECT a
        FROM Address a
        JOIN FETCH a.country
        JOIN FETCH a.province
        JOIN FETCH a.ward
        WHERE a.id = :id
        """)
    Optional<Address> findWithInfoById(@Param("id") UUID id);
}
