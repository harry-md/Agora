package com.agora.address.application;

import com.agora.address.AddressRequest;
import com.agora.address.AddressResponse;
import com.agora.address.AddressService;
import com.agora.address.domain.Address;
import com.agora.address.domain.Country;
import com.agora.address.domain.Province;
import com.agora.address.domain.Ward;
import com.agora.address.persistence.AddressRepository;
import com.agora.address.persistence.CountryRepository;
import com.agora.address.persistence.ProvinceRepository;
import com.agora.address.persistence.WardRepository;
import com.agora.exception.BadRequestException;
import com.agora.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final WardRepository wardRepository;
    private final ProvinceRepository provinceRepository;
    private final CountryRepository countryRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddress(UUID id) {
        return addressMapper.toResponse(find(id));
    }

    @Override
    @Transactional
    public UUID addAddress(AddressRequest request) {
        Address address = addressMapper.toEntity(request);
        applyLocation(address, request);
        return addressRepository.save(address).getId();
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID id, AddressRequest request) {
        Address address = find(id);
        addressMapper.updateEntity(request, address);
        applyLocation(address, request);
        return addressMapper.toResponse(address);
    }

    private Address find(UUID id) {
        return addressRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));
    }

    private void applyLocation(Address address, AddressRequest request) {
        Ward ward = wardRepository
                .findById(request.wardId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phường/xã"));
        Province province = provinceRepository
                .findById(request.provinceId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tỉnh/thành phố"));
        Country country = countryRepository
                .findById(request.countryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quốc gia"));

        if (!ward.getProvince().getId().equals(province.getId())) {
            throw new BadRequestException("Phường/xã không thuộc tỉnh/thành phố đã chọn");
        }
        if (!province.getCountry().getId().equals(country.getId())) {
            throw new BadRequestException("Tỉnh/thành phố không thuộc quốc gia đã chọn");
        }
        address.setWard(ward);
        address.setProvince(province);
        address.setCountry(country);
    }
}
