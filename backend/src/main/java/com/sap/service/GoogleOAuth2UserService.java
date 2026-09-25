package com.sap.service;

import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuth2UserService extends OidcUserService {

    private final UsuarioRepository usuarioRepository;

    public GoogleOAuth2UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        String provedorId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String nome = oidcUser.getFullName();

        usuarioRepository
                .findByProvedorAndProvedorId(
                        ProvedorAutenticacao.GOOGLE,
                        provedorId
                )
                .orElseGet(() -> criarUsuarioGoogle(
                        nome,
                        email,
                        provedorId
                ));

        return oidcUser;
    }

    private Usuario criarUsuarioGoogle(
            String nome,
            String email,
            String provedorId
    ) {

        if (email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "O Google não forneceu um e-mail para o usuário."
            );
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalStateException(
                    "Já existe uma conta cadastrada com este e-mail."
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNome(
                nome != null && !nome.isBlank()
                        ? nome
                        : emailNormalizado
        );

        usuario.setEmail(emailNormalizado);
        usuario.setSenha(null);

        usuario.setTipo("ESTUDANTE");

        usuario.setProvedor(ProvedorAutenticacao.GOOGLE);
        usuario.setProvedorId(provedorId);

        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
    }
}
