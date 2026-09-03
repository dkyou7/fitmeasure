package com.iamnot.fitmeasure.owner.dto;

import com.iamnot.fitmeasure.membership.MembershipRole;
import com.iamnot.fitmeasure.membership.MembershipStatus;
import java.time.LocalDate;

public record StaffRow(
        Long membershipId,
        String nickname,
        MembershipRole role,     // OWNER / STAFF
        MembershipStatus status,
        boolean isOwner,
        LocalDate joinedAt
) {}