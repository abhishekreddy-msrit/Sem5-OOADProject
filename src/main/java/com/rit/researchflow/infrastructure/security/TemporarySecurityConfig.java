package com.rit.researchflow.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary permit-all configuration so the application can start during the
 * initial setup phase. Real session authentication and role rules will replace
 * this in a later implementation step. This is not the final security model.
 */
@Configuration
@EnableWebSecurity
public class TemporarySecurityConfig {

    @Bean
    SecurityFilterChain temporarySecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(Customizer.withDefaults())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());
        return http.build();
    }
}
