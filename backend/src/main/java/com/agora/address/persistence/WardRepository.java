package com.agora.address.persistence;

import com.agora.address.domain.Ward;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WardRepository extends JpaRepository<Ward, UUID> {}
