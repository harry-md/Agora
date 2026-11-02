package com.agora.warranty;

import java.util.List;
import java.util.UUID;

public interface WarrantyPolicyService {
    WarrantyPolicyResponse create(CreateWarrantyPolicyRequest request);

    WarrantyPolicyResponse getCurrentVersion(String code);

    WarrantyPolicyResponse getByVersion(String code, int version);

    List<WarrantyPolicyResponse> getAll(String code);

    boolean existsById(UUID id);
}
