package com.agora.warranty.application;

import com.agora.exception.BadRequestException;
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

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
class WarrantyPolicyServiceImpl implements WarrantyPolicyService {
    private static final Pattern DRIVE_FILE = Pattern.compile("^/file/d/([A-Za-z0-9_-]+)");
    private final WarrantyPolicyRepository warrantyPolicyRepository;
    private final WarrantyPolicyMapper warrantyPolicyMapper;
    private final TransactionTemplate transactionTemplate;

    /**
     * 1. Check the URL if it is gg drive link
     * 2. Reformat the url
     * 3. Check existed
     */
    @Override
    public WarrantyPolicyResponse createWarrantyPolicy(CreateWarrantyPolicyRequest request) {
        URI uri = URI.create(request.documentKey());
        String host = uri.getHost() == null ? null : uri.getHost().toLowerCase(Locale.ROOT);
        Matcher m = DRIVE_FILE.matcher(uri.getPath() == null ? "" : uri.getRawPath());
        if (!"drive.google.com".equals(host) || !m.find()) {
            throw new BadRequestException("Link chính sách phải là link Google Drive");
        }
        String documentKey = "https://drive.google.com/file/d/" + m.group(1) + "/view";
        if (this.warrantyPolicyRepository.existsByDocumentKey(documentKey)) {
            throw new ConflictException("Link tài liệu đã được dùng cho một chính sách khác");
        }
        try {
            return transactionTemplate.execute(status -> {
                int nextVersion = this.warrantyPolicyRepository
                                .findMaxVersionByCode(request.code())
                                .orElse(0)
                        + 1;
                WarrantyPolicy policy = this.warrantyPolicyMapper.toEntity(request);
                policy.setVersion(nextVersion);
                policy.setDocumentKey(documentKey);
                return this.warrantyPolicyMapper.toResponse(warrantyPolicyRepository.save(policy));
            });
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Chính sách bảo hành vừa được cập nhật, vui lòng thử lại");
        }
    }

    @Override
    public WarrantyPolicyResponse getCurrentByCode(String code) {
        return this.warrantyPolicyRepository
                .findFirstByCodeAndEffectiveFromLessThanEqualOrderByVersionDesc(
                        code.strip().toUpperCase(Locale.ROOT), Instant.now())
                .map(this.warrantyPolicyMapper::toResponse)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy chính sách bảo hành"));
    }

    @Override
    public WarrantyPolicyResponse getByCodeAndVersion(String code, int version) {
        return this.warrantyPolicyRepository
                .findByCodeAndVersion(code.strip().toUpperCase(Locale.ROOT), version)
                .map(this.warrantyPolicyMapper::toResponse)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy chính sách bảo hành"));
    }

    @Override
    public List<WarrantyPolicyResponse> getAllByCode(String code) {
        List<WarrantyPolicy> policies =
                this.warrantyPolicyRepository.findAllByCodeOrderByVersionDesc(
                        code.strip().toUpperCase(Locale.ROOT));
        if (policies.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy chính sách bảo hành");
        }
        return policies.stream().map(this.warrantyPolicyMapper::toResponse).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return this.warrantyPolicyRepository.existsById(id);
    }
}
