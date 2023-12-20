package fr.minint.sgin.attestationvalidatorapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable(); // NOSONAR
        http.headers().cacheControl().and().contentTypeOptions().and().xssProtection().and().frameOptions()
            .and().httpStrictTransportSecurity().disable();
        return http.build();
    }

}
