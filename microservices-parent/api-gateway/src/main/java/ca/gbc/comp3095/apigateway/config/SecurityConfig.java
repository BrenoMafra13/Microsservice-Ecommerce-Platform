package ca.gbc.comp3095.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.client.reactive.AbstractClientHttpConnectorProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        log.info("Initializing Security Filter Chain");

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable) //Disable CSRF (temporarily)
                // For now, allow everything through without auth. Re-enable JWT when auth is ready.
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .build();

    }
}
