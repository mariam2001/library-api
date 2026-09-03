package com.library.library_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// ⚠️ TEMPORARY / DEVELOPMENT SECURITY POSTURE ⚠️
// This config exists only to UNBLOCK building and testing the API. It deliberately
// leaves everything open. Proper security (real users/roles, protected routes, login)
// is its own milestone - replace this before the project is considered "done".
//
// Why this file is needed at all: the moment `spring-boot-starter-security` is on the
// classpath, Spring Boot auto-applies a default that (a) protects EVERY endpoint with a
// single generated user, and (b) enables CSRF, which blocks all POST/PUT/DELETE. Defining
// our own SecurityFilterChain bean OVERRIDES that default with the rules below.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // A SecurityFilterChain bean is how you customize Spring Security. Spring injects a
    // pre-built HttpSecurity object; we configure it and return the built chain.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection. CSRF defends browser apps that rely on session
                // cookies - a malicious site can't forge a request if it can't read a token.
                // A stateless REST API (called by Postman/curl/another service, not a cookie
                // session) doesn't use that model, so CSRF just gets in the way. Disabling it
                // is the standard choice for this kind of API - and it's exactly what was
                // causing the 401 on every POST.
                .csrf(csrf -> csrf.disable())
                // Authorization rules: for now, allow every request through with no login.
                // Later (security milestone) this becomes something like:
                //   auth.requestMatchers("/api/about").permitAll()
                //       .anyRequest().authenticated()
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
