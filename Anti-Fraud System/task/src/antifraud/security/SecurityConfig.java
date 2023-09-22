package antifraud.security;

import antifraud.model.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.logging.Logger;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    static class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
        Logger logger = Logger.getLogger(RestAuthenticationEntryPoint.class.getName());

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        }
    }

    @Bean
    RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException exception) {
        return new ResponseEntity<>("You do not have access to this resource: " + exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .exceptionHandling(handing -> handing
                        .authenticationEntryPoint(this.restAuthenticationEntryPoint())
                )
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(Role.ADMINISTRATOR.name(), Role.SUPPORT.name())
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasAnyRole(Role.ADMINISTRATOR.name())
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access").hasAnyRole(Role.ADMINISTRATOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasAnyRole(Role.MERCHANT.name())
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasAnyRole(Role.SUPPORT.name())
                        .requestMatchers("/api/antifraud/suspicious-ip").hasAnyRole(Role.SUPPORT.name())
                        .requestMatchers("/api/antifraud/suspicious-ip/**").hasAnyRole(Role.SUPPORT.name())
                        .requestMatchers("/api/antifraud/stolencard").hasAnyRole(Role.SUPPORT.name())
                        .requestMatchers("/api/antifraud/stolencard/**").hasAnyRole(Role.SUPPORT.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasAnyRole(Role.ADMINISTRATOR.name())
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history").hasAnyRole(Role.SUPPORT.name())
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history/**").hasAnyRole(Role.SUPPORT.name())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers -> headers.frameOptions().disable())
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
