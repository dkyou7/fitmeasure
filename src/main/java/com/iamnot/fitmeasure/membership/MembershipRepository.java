package com.iamnot.fitmeasure.membership;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByClubIdAndRoleOrderByNicknameAsc(Long clubId, MembershipRole role);

    Optional<Membership> findByIdAndClubId(Long id, Long clubId);

    long countByClubIdAndRole(Long clubId, MembershipRole role);

    boolean existsByClubIdAndMemberId(Long clubId, Long memberId);

    @Query("""
        select m from Membership m
        join m.member mb
        where mb.phone = :phone
          and mb.passwordHash is not null
          and m.role in (com.iamnot.fitmeasure.membership.MembershipRole.OWNER,
                         com.iamnot.fitmeasure.membership.MembershipRole.STAFF)
    """)
    List<Membership> findLoginableByPhone(@Param("phone") String phone);
}