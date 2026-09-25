package com.sap.repository;

import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByProvedorAndProvedorId(
            ProvedorAutenticacao provedor,
            String provedorId
    );
}
