package com.example.cego_webapp;

import com.example.cego_webapp.models.Usuario;
import com.example.cego_webapp.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CegoWebappApplication {

	public static void main(String[] args) {
		SpringApplication.run(CegoWebappApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Crie um usuário com um login e senha
			if (usuarioRepository.findByUsername("admin").isEmpty()) {
				Usuario user = new Usuario("admin", passwordEncoder.encode("admin"));
				usuarioRepository.save(user);
				System.out.println(">>> Usuário 'cego' criado com sucesso! <<<");
			}
		};
	}

}
