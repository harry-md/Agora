package com.agora.address.persistence;

import com.agora.address.domain.Province;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProvinceRepository extends JpaRepository<Province, UUID> {}
