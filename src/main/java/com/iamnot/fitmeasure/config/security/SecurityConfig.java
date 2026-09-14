package com.iamnot.fitmeasure.config.security;

import com.iamnot.fitmeasure.api.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, e) -> res.sendError(401)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup","/contact","/connect",
                                "/s/**", "/css/**", "/js/**",
                                "/favicon.ico", "/error", "/h2-console/**").permitAll()
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            String next = request.getParameter("next");
                            // login 폼이 next를 hidden으로 넘겨줘야 함
                            response.sendRedirect(next != null && next.startsWith("/") ? next : "/");
                        })
                        .permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login"))
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        return http.build();
    }
}