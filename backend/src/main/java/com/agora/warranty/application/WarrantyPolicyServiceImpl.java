package com.agora.warranty.application;

import com.agora.exception.ConflictException;
import com.agora.exception.ResourceNotFoundException;
import com.agora.warranty.CreateWarrantyPolicyRequest;
import com.agora.warranty.WarrantyPolicyResponse;
import com.agora.warranty.WarrantyPolicyService;
import com.agora.warranty.domain.WarrantyPolicy;
import com.agora.warranty.persistence.WarrantyPolicyRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
class WarrantyPolicyServiceImpl implements WarrantyPolicyService {
    private static final Pattern DRIVE_FILE = Pattern.compile("^/file/d/([A-Za-z0-9_-]+)");
    private final WarrantyPolicyRepository warrantyPolicyRepository;
    private final WarrantyPolicyMapper warrantyPolicyMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public WarrantyPolicyResponse create(CreateWarrantyPolicyRequest request) {
        String documentKey = request.documentKey();
        if (warrantyPolicyRepository.existsByDocumentKey(documentKey)) {
            throw new ConflictException("Link tài liệu đã được dùng cho một chính sách khác");
        }
        try {
            return transactionTemplate.execute(status -> {
                String code = request.code().strip().toUpperCase(Locale.ROOT);
                int nextVersion =
                        warrantyPolicyRepository.findMaxVersionByCode(code).orElse(0) + 1;
                WarrantyPolicy policy = warrantyPolicyMapper.toEntity(request);
                policy.setCode(code);
                policy.setVersion(nextVersion);
                return warrantyPolicyMapper.toResponse(warrantyPolicyRepository.save(policy));
            });
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Chính sách bảo hành vừa được cập nhật, vui lòng thử lại");
        }
    }

    @Override
    public WarrantyPolicyResponse getCurrentVersion(String code) {
        return warrantyPolicyRepository
                .findFirstByCodeAndEffectiveFromLessThanEqualOrderByVersionDesc(
                        code.strip().toUpperCase(Locale.ROOT), Instant.now())
                .map(warrantyPolicyMapper::toResponse)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy chính sách bảo hành"));
    }

    @Override
    public WarrantyPolicyResponse getByVersion(String code, int version) {
        return warrantyPolicyRepository
                .findByCodeAndVersion(code.strip().toUpperCase(Locale.ROOT), version)
                .map(warrantyPolicyMapper::toResponse)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy chính sách bảo hành"));
    }

    @Override
    public List<WarrantyPolicyResponse> getAll(String code) {
        List<WarrantyPolicy> policies = warrantyPolicyRepository.findAllByCodeOrderByVersionDesc(
                code.strip().toUpperCase(Locale.ROOT));
        if (policies.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy chính sách bảo hành");
        }
        return policies.stream().map(warrantyPolicyMapper::toResponse).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return warrantyPolicyRepository.existsById(id);
    }
}
