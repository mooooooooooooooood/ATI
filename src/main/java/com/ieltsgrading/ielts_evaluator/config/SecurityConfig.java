package com.ieltsgrading.ielts_evaluator.config;

import com.ieltsgrading.ielts_evaluator.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        // ⭐ CRITICAL FIX: Ignore CSRF token validation for the asynchronous POST endpoint
                        .ignoringRequestMatchers("/reading/tests/get-explanation")
                )
                .authorizeHttpRequests(auth -> auth
                        // Public resources
                        .requestMatchers("/", "/home", "/index").permitAll()
                        .requestMatchers("/user/login", "/user/signup", "/user/register").permitAll()
                        .requestMatchers("/user/forgot-password", "/user/reset-password").permitAll()
                        .requestMatchers("/user/verify-email").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Explicitly permit the explanation endpoint
                        .requestMatchers(HttpMethod.POST, "/reading/tests/get-explanation").permitAll()

                        // Require login redirect
                        .requestMatchers("/require-login").permitAll()

                        // Protected resources
                        .requestMatchers("/dashboard/**", "/profile/**").authenticated()
                        .requestMatchers("/test/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .loginProcessingUrl("/user/login")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/user/login?error=true")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/user/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(86400) // 24 hours
                        .userDetailsService(userDetailsService))
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/error/403"))
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/user/login?expired=true"));

        return http.build();
    }
}