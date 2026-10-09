package com.agora.address.persistence;

import com.agora.address.domain.Address;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {}
