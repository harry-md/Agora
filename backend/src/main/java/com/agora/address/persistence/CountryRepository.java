package com.agora.address.persistence;

import com.agora.address.domain.Country;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {

    @Query("SELECT c.name FROM Country c WHERE c.id = :id")
    Optional<String> findNameById(UUID id);
}
