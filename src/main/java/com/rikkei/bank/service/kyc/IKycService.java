package com.rikkei.bank.service.kyc;

import com.rikkei.bank.dto.kyc.request.KycRequest;
import com.rikkei.bank.dto.kyc.response.KycResponse;
import com.rikkei.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface IKycService {

    KycResponse submitKyc(KycRequest request, User user) throws IOException;

    KycResponse approveKyc(Long kycId, boolean approved, String rejectReason, User staffUser);

    Page<KycResponse> getPendingKyc(Pageable pageable);

    KycResponse getMyKycStatus(User user);
}