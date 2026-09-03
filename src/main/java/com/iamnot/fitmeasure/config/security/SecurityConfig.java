package com.iamnot.fitmeasure.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSecurity filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 공개: 공유 카드·claim, 로그인, 정적 리소스, H2 콘솔
                .requestMatchers("/s/**", "/login", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("phone")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
            )
            // H2 콘솔 iframe 허용 (개발용)
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            // 폼 POST 많고 MVP라 CSRF는 h2/공유만 예외… 실은 켜두는 게 맞음(아래 주석)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        return http;
    }
}