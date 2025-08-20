package com.example.cego_webapp.config;

import com.example.cego_webapp.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Libera o acesso a recursos estáticos (CSS, JS) e à página de login
                        .requestMatchers("/css/**", "/js/**", "/login").permitAll()
                        // EXIGE autenticação para qualquer outra URL do sistema
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")           // Nossa página de login customizada
                        .defaultSuccessUrl("/", true) // Para onde ir após o login
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // Para onde ir após sair
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Define como o Spring Security vai buscar os usuários.
     * Em vez de uma classe Service, definimos aqui para simplificar.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o login: " + username));
    }

    /**
     * Define o algoritmo para codificar senhas. Essencial para a segurança.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}