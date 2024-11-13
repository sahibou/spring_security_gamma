package com.gammaray.demo_gamma.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SimpleSecurityFilterChainConfiguration
{
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    //org.springframework.security.web INFO to see filters called
    http
        .csrf(Customizer.withDefaults())
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().authenticated()
        )
        .httpBasic(Customizer.withDefaults())
        .formLogin(Customizer.withDefaults());
    return http.build();
  }
}
