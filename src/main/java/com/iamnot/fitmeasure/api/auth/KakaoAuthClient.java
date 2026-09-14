package com.iamnot.fitmeasure.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 앱이 받아온 인가 코드를 카카오 토큰으로 교환하고 고유 id를 얻는다.
 * client secret이 필요하므로 반드시 서버에서 수행한다.
 */
@Component
@RequiredArgsConstructor
public class KakaoAuthClient {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestClient restClient = RestClient.create();

    /** 인가 코드 → 카카오 사용자 고유 id */
    public String exchangeAndGetProviderId(String code, String redirectUri) {
        ClientRegistration kakao = clientRegistrationRepository.findByRegistrationId("kakao");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakao.getClientId());
        form.add("client_secret", kakao.getClientSecret());
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        Map<?, ?> token = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        String accessToken = String.valueOf(token.get("access_token"));

        Map<?, ?> me = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        return String.valueOf(me.get("id"));
    }
}