package com.agora.address.application;

import com.agora.address.CountryService;
import com.agora.address.persistence.CountryRepository;
import com.agora.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    @Override
    public boolean existsById(UUID id) {
        return countryRepository.existsById(id);
    }

    @Override
    public String getNameById(UUID id) {
        return countryRepository
                .findNameById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quốc gia"));
    }
}
