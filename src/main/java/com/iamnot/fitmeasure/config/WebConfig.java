package com.iamnot.fitmeasure.config;

import com.iamnot.fitmeasure.onboard.OnboardingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final OnboardingInterceptor onboardingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(onboardingInterceptor)
                .addPathPatterns("/**")           // 전체 적용
                .excludePathPatterns(              // 정적·인증·공개 경로 제외
                        "/css/**", "/js/**", "/images/**", "/favicon.ico",
                        "/login", "/signup", "/oauth2/**", "/login/oauth2/**",
                        "/", "/contact", "/s/**", "/connect",
                        "/h2-console/**", "/error"
                );
    }
}