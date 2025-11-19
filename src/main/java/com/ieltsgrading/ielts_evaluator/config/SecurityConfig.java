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
                        // ⭐ CRITICAL FIX: Ignore CSRF for all async submission endpoints, including the new bulk review endpoint.
                        .ignoringRequestMatchers(
                                "/reading/tests/get-explanation",
                                "/reading/tests/get-test-review/**", // <-- Existing Reading Fix
                                // --- LISTENING ENDPOINTS ADDED ---
                                "/listening/tests/submit",          // Form Submission (POST)
                                "/listening/tests/get-test-review/**", // Gemini Review (POST/AJAX)
                                // ---------------------------------
                                "/api/upload-audio",
                                "/speaking/next"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Public resources
                        .requestMatchers("/", "/home", "/index").permitAll()
                        .requestMatchers("/user/login", "/user/signup", "/user/register").permitAll()
                        .requestMatchers("/user/forgot-password", "/user/reset-password").permitAll()
                        .requestMatchers("/user/verify-email").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                        .requestMatchers("/error", "/error/**").permitAll()

                        // Explicitly permit the new bulk review endpoint (POST method)
                        .requestMatchers(HttpMethod.POST, "/reading/tests/get-test-review/**").permitAll() // <-- Existing Reading Fix
                        // --- LISTENING POST METHOD ADDED ---
                        .requestMatchers(HttpMethod.POST, "/listening/tests/get-test-review/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/listening/tests/submit").authenticated()
                        // ------------------------------------

                        // Obsolete endpoint left for safety, but will be removed eventually
                        .requestMatchers(HttpMethod.POST, "/reading/tests/get-explanation").permitAll()

                        // Protected speaking test endpoints
                        .requestMatchers("/speaking/**").authenticated()
                        .requestMatchers("/test/**").authenticated() // Generic entry point

                        // File Upload API
                        .requestMatchers(HttpMethod.POST, "/api/upload-audio").authenticated()

                        // Require login redirect
                        .requestMatchers("/require-login").permitAll()

                        // --- PROTECTED LISTENING ENDPOINTS ADDED ---
                        // Needs authentication for the listing page and detail pages
                        .requestMatchers("/listening/tests", "/listening/tests/**").authenticated()
                        // ---------------------------------------------

                        // Protected resources
                        .requestMatchers("/dashboard/**", "/profile/**").authenticated()
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