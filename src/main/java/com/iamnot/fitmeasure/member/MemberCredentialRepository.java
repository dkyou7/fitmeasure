package com.iamnot.fitmeasure.member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberCredentialRepository extends JpaRepository<MemberCredential, Long> {
    Optional<MemberCredential> findByProviderAndProviderId(AuthProvider provider, String providerId);
}