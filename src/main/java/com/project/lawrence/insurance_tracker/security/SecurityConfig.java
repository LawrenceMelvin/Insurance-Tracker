package com.project.lawrence.insurance_tracker.security;

import com.project.lawrence.insurance_tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**","/auth/register","/css/**","/js/**").permitAll() // Allow access to H2 console
                        .requestMatchers("/","/home").authenticated() // Allow access after login
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")  // Custom login page
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true") // Redirect to login page with error
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // Custom logout URL)
                        .logoutSuccessUrl("/login?logout") // Redirect to login page after logout
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")) // Disable CSRF for H2
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)); // Allow frames for H2 console

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
