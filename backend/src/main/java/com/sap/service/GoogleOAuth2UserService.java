package com.sap.service;

import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class GoogleOAuth2UserService extends OidcUserService {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public GoogleOAuth2UserService(
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService) {

        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        String provedorId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String nome = oidcUser.getFullName();

        Usuario usuario = usuarioRepository
                .findByProvedorAndProvedorId(
                        ProvedorAutenticacao.GOOGLE,
                        provedorId
                )
                .orElseGet(() -> criarUsuarioGoogle(
                        nome,
                        email,
                        provedorId
                ));

        auditoriaService.registrar(
                usuario.getId(),
                usuario.getEmail(),
                "LOGIN_GOOGLE",
                "Login realizado com sucesso utilizando Google OAuth."
        );

        Set<SimpleGrantedAuthority> authorities =
                new HashSet<>();

        oidcUser.getAuthorities().forEach(authority ->
                authorities.add(
                        new SimpleGrantedAuthority(
                                authority.getAuthority()
                        )
                )
        );

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_" + usuario.getTipo()
                )
        );

        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "sub"
        );
    }

    private Usuario criarUsuarioGoogle(
            String nome,
            String email,
            String provedorId) {

        if (email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "O Google não forneceu um e-mail para o usuário."
            );
        }

        String emailNormalizado =
                email.trim().toLowerCase();

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

        Usuario usuarioSalvo =
                usuarioRepository.save(usuario);

        auditoriaService.registrar(
                usuarioSalvo.getId(),
                usuarioSalvo.getEmail(),
                "CADASTRO_GOOGLE",
                "Conta criada utilizando Google OAuth."
        );

        return usuarioSalvo;
    }
}