package ca.gbc.comp3095.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.client.reactive.AbstractClientHttpConnectorProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        log.info("Initializing Security Filter Chain");

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)   //Disable CSRF (temporarily)
                //Authorize all HTTP request, requiring authentication
                .authorizeHttpRequests(
                        authorize -> authorize.anyRequest().authenticated() )
                // Permit  all unauthenticated requests
                //.authorizeHttpRequests(
                //                        authorize -> authorize.annyRequest.permittedAll() )
                //Set up OAuth2 server tro use JWT token for authentication
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();

    }
}
