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
        String registrationId = req.getClientRegistration().getRegistrationId(); // "kakao" | "naver"

        OAuthInfo info = extract(registrationId, oauth.getAttributes());

        Member member = credentialRepository
                .findByProviderAndProviderId(info.provider(), info.providerId())
                .map(MemberCredential::getMember)
                .orElseGet(() -> createSocialMember(info));

        List<Membership> memberships =
                membershipRepository.findLoginableByMemberId(member.getId());
        Membership chosen = memberships.stream()
                .filter(m -> m.getRole() == MembershipRole.OWNER)
                .findFirst()
                .orElse(memberships.isEmpty() ? null : memberships.get(0));

        return new OAuth2LoginMember(member, chosen, oauth.getAttributes());
    }

    /** provider별 응답 구조에서 고유 id·닉네임을 뽑는다. */
    private OAuthInfo extract(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> {
                String id = String.valueOf(attributes.get("id"));
                String nickname = "카카오회원";
                if (attributes.get("properties") instanceof Map<?, ?> p && p.get("nickname") != null) {
                    nickname = String.valueOf(p.get("nickname"));
                }
                yield new OAuthInfo(AuthProvider.KAKAO, id, nickname);
            }
            case "naver" -> {
                // 네이버는 사용자 정보가 response 객체 안에 중첩
                Map<?, ?> resp = (Map<?, ?>) attributes.get("response");
                String id = String.valueOf(resp.get("id"));
                String nickname = resp.get("name") != null ? String.valueOf(resp.get("name")) : "네이버회원";
                yield new OAuthInfo(AuthProvider.NAVER, id, nickname);
            }
            default -> throw new IllegalStateException("지원하지 않는 소셜 로그인: " + registrationId);
        };
    }

    private Member createSocialMember(OAuthInfo info) {
        Member m = Member.create();
        memberRepository.save(m);
        credentialRepository.save(
                MemberCredential.social(m, info.provider(), info.providerId(), null));
        return m;
    }

    private record OAuthInfo(AuthProvider provider, String providerId, String nickname) {}
}