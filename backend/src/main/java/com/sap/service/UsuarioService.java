package com.sap.service;

import com.sap.dto.CadastroUsuarioRequest;
import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UsuarioService {

    private static final String VERSAO_ATUAL_TERMOS = "1.0";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(CadastroUsuarioRequest request) {

        String emailNormalizado =
                request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        if (!request.isAceitouTermos()) {
            throw new IllegalArgumentException(
                    "É necessário aceitar os Termos de Uso e a Política de Privacidade."
            );
        }

        String senhaCriptografada =
                passwordEncoder.encode(request.getSenha());

        Usuario usuario = new Usuario();

        usuario.setNome(request.getNome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(senhaCriptografada);

        usuario.setTipo("ESTUDANTE");
        usuario.setProvedor(ProvedorAutenticacao.LOCAL);
        usuario.setProvedorId(null);
        usuario.setAtivo(true);

        usuario.setAceitouTermos(true);
        usuario.setDataAceiteTermos(LocalDateTime.now());
        usuario.setVersaoTermos(VERSAO_ATUAL_TERMOS);

        return usuarioRepository.save(usuario);
    }
}