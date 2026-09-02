package com.iamnot.fitmeasure.membership;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByClubIdAndRoleOrderByNicknameAsc(Long clubId, MembershipRole role);

    Optional<Membership> findByIdAndClubId(Long id, Long clubId);

    long countByClubIdAndRole(Long clubId, MembershipRole role);

    boolean existsByClubIdAndMemberId(Long clubId, Long memberId);
}