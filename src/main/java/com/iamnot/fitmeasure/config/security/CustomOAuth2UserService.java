package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.member.*;
import com.iamnot.fitmeasure.membership.Membership;
import com.iamnot.fitmeasure.membership.MembershipRepository;
import com.iamnot.fitmeasure.membership.MembershipRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final MemberCredentialRepository credentialRepository;
    private final MembershipRepository membershipRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oauth = super.loadUser(req);
        String kakaoId = String.valueOf(oauth.getAttributes().get("id"));

        final String nickname = extractNickname(oauth.getAttributes());

        Member member = credentialRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, kakaoId)
                .map(MemberCredential::getMember)
                .orElseGet(() -> createKakaoMember(kakaoId, nickname));

        // 폼 로그인과 동일: 로그인 가능한 소속 중 OWNER 우선, 없으면 첫 번째, 아무것도 없으면 null
        List<Membership> memberships =
                membershipRepository.findLoginableByMemberId(member.getId());
        Membership chosen = memberships.stream()
                .filter(m -> m.getRole() == MembershipRole.OWNER)
                .findFirst()
                .orElse(memberships.isEmpty() ? null : memberships.get(0));

        return new OAuth2LoginMember(member, chosen, oauth.getAttributes());
    }

    private String extractNickname(Map<String, Object> attributes) {
        Object props = attributes.get("properties");
        if (props instanceof Map<?, ?> p && p.get("nickname") != null) {
            return String.valueOf(p.get("nickname"));
        }
        return "카카오회원";
    }

    private Member createKakaoMember(String kakaoId, String nickname) {
        Member m = Member.anonymous();
        m.claim(nickname);
        memberRepository.save(m);
        credentialRepository.save(MemberCredential.social(m, AuthProvider.KAKAO, kakaoId, null));
        return m;
    }
}