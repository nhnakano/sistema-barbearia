package com.barbearia.sistema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // LIBERADO: Tudo que o cliente precisa ver
                .requestMatchers("/", "/agendar", "/confirmar-agendamento", "/horarios-disponiveis", "/css/**", "/js/**").permitAll()
                // TRANCADO: O painel e as rotas de admin
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .defaultSuccessUrl("/painel", true) // Após logar, vai direto pro painel
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutSuccessUrl("/agendar") // Ao sair, volta pra tela de agendar
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Necessário para h2 e formulários simples em dev

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        // Usuário: admin | Senha: admin123 (Criptografada com BCrypt)
        UserDetails user = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}