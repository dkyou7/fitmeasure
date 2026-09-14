package com.iamnot.fitmeasure.api.auth;

import com.iamnot.fitmeasure.member.AuthProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/** 앱이 보낸 소셜 액세스 토큰을 provider에 확인해 고유 id를 얻는다. */
@Component
public class SocialTokenVerifier {

    private final RestClient restClient = RestClient.create();

    public String verifyAndGetProviderId(AuthProvider provider, String accessToken) {
        return switch (provider) {
            case KAKAO -> {
                Map<?, ?> body = restClient.get()
                        .uri("https://kapi.kakao.com/v2/user/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .body(Map.class);
                yield String.valueOf(body.get("id"));
            }
            case NAVER -> {
                Map<?, ?> body = restClient.get()
                        .uri("https://openapi.naver.com/v1/nid/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .body(Map.class);
                Map<?, ?> resp = (Map<?, ?>) body.get("response");
                yield String.valueOf(resp.get("id"));
            }
            default -> throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
        };
    }
}