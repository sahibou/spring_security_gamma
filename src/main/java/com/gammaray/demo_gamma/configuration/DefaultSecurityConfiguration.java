package com.gammaray.demo_gamma.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@EnableWebSecurity //enable default spring security filterchain
@Configuration 
@Profile("reactivateme")
public class DefaultSecurityConfiguration
{
  @Bean
  @ConditionalOnMissingBean(UserDetailsService.class) // UserDetailsService is used by daoAuthenticationProvider
  //as user password source, this instruction publishes a custom UserDetailService if none exists.
  InMemoryUserDetailsManager inMemoryUserDetailsManager() {
    String generatedPassword = "uuu";
    return new InMemoryUserDetailsManager(User.withUsername("user")
                                              .password(generatedPassword).roles("USER").build());
  }

  @Bean
  @ConditionalOnMissingBean(AuthenticationEventPublisher.class)
  //publishes auth events, AuthenticationSuccessEvent or AuthenticationFailureEvent fired
  DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(ApplicationEventPublisher delegate) {
    return new DefaultAuthenticationEventPublisher(delegate);//send the event to  components with :
//    @EventListener
//    public void onSuccess(AuthenticationSuccessEvent success) {
//      ...
    
    
  }
  
  //DelegatingFilterProxy is used to put a spring filter bean into the servlet bean lifecycle. (adapter)
  //SecurityFilterChain is used to find and call all security filters to use. and clear security context
  //you can have two separate filterchains bc each defines a mattching pattern : /api/**  /message/**
}