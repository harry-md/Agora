package com.agora.warranty;

import java.util.List;
import java.util.UUID;

public interface WarrantyPolicyService {
    WarrantyPolicyResponse createWarrantyPolicy(CreateWarrantyPolicyRequest request);

    WarrantyPolicyResponse getCurrentByCode(String code);

    WarrantyPolicyResponse getByCodeAndVersion(String code, int version);

    List<WarrantyPolicyResponse> getAllByCode(String code);

    boolean existsById(UUID id);
}
