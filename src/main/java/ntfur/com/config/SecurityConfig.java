package ntfur.com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // cho phép tất cả
            )
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())   // ❗ tắt login form
            .httpBasic(basic -> basic.disable()); // ❗ tắt basic auth

        return http.build();
    }
}
