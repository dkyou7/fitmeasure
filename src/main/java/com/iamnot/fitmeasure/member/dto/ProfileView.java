package com.iamnot.fitmeasure.member.dto;

public record ProfileView(
        String name,          // Member.name (없으면 null)
        String nickname,      // 현재 클럽 닉네임 (클럽 없으면 null)
        String phone,         // 연락처 (마스킹해서 보여줄 수도)
        boolean hasClub       // 클럽 소속 여부
) {}