package com.agora.address.persistence;

import com.agora.address.domain.Address;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    @EntityGraph(attributePaths = {"ward", "province", "country"})
    Optional<Address> findById(UUID id);
}
