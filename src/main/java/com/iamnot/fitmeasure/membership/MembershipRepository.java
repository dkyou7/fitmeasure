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
        where m.member.id = :memberId
          and m.role in (com.iamnot.fitmeasure.membership.MembershipRole.OWNER,
                         com.iamnot.fitmeasure.membership.MembershipRole.STAFF)
    """)
    List<Membership> findLoginableByMemberId(@Param("memberId") Long memberId);

    @Query("""
    select count(m) from Membership m
    where m.club.id = :clubId
      and m.role = com.iamnot.fitmeasure.membership.MembershipRole.MEMBER
      and m.member.claimedAt is not null
""")
    long countClaimedMembers(@Param("clubId") Long clubId);

    List<Membership> findByMemberIdAndRole(Long memberId, MembershipRole role);

    Optional<Membership> findByMemberIdAndClubId(Long memberId, Long clubId);

    boolean existsByClubIdAndPhone(Long clubId, String phone);
}