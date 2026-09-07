package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.member.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final MemberCredentialRepository credentialRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oauth = super.loadUser(req);
        String kakaoId = String.valueOf(oauth.getAttributes().get("id"));

        // 카카오 닉네임 추출 (한 번만 할당 → effectively final)
        final String nickname = extractNickname(oauth.getAttributes());

        Member member = credentialRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, kakaoId)
                .map(MemberCredential::getMember)
                .orElseGet(() -> createKakaoMember(kakaoId, nickname));

        return new OAuth2LoginMember(member, oauth.getAttributes());
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
        m.claim(nickname);   // 이름에 카카오 닉네임
        memberRepository.save(m);
        credentialRepository.save(MemberCredential.social(m, AuthProvider.KAKAO, kakaoId, null));
        return m;
    }
}