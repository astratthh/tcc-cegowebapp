package com.example.cego_webapp.repositories;

import com.example.cego_webapp.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Metodo essencial para o Spring Security encontrar o usuário pelo login.
    Optional<Usuario> findByUsername(String username);
}