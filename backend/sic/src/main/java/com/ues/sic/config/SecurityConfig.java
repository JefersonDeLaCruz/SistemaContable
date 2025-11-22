package com.ues.sic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.ues.sic.usuarios.CustomUserDetailsService;

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
  // Permitir acceso público a recursos estáticos y páginas de login
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "CONTADOR", "AUDITOR")
                .requestMatchers("/api/cuentas/**").hasAnyRole("ADMIN", "CONTADOR", "AUDITOR")
                .requestMatchers("/api/periodos/**").hasAnyRole("ADMIN", "CONTADOR", "AUDITOR")
                .requestMatchers("/api/libromayor/**").hasAnyRole("ADMIN", "CONTADOR", "AUDITOR")
                .requestMatchers("/api/documentos/**").hasAnyRole("ADMIN", "CONTADOR", "AUDITOR")
                .requestMatchers("/api/partidas/**").hasAnyRole("ADMIN", "CONTADOR", "AUDITOR")
                .requestMatchers("/api/tipos-documento/**").hasAnyRole("ADMIN", "CONTADOR", "AUDITOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/auditor/**").hasAnyRole("AUDITOR", "ADMIN")
                .requestMatchers("/contador/**").hasRole("CONTADOR")
                .requestMatchers("/dashboard").authenticated()
                // Requerir autenticación para todas las demás rutas
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // Usar el login formal
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            // Deshabilitar CSRF para APIs ojo que al desaghbilitar CSRF postman no funciona
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            // Configurar headers de seguridad para evitar cache
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .frameOptions(frame -> frame.sameOrigin())
                .httpStrictTransportSecurity(hstsHeaderWriter -> hstsHeaderWriter.disable())
            );

        return http.build();
    }
}
