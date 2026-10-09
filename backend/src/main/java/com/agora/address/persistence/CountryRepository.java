package com.agora.address.persistence;

import com.agora.address.domain.Country;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {}
