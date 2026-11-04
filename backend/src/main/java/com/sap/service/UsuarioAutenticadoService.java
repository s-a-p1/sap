package com.sap.service;

import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> buscar(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {

            String registrationId =
                    oauthToken.getAuthorizedClientRegistrationId();

            OAuth2User oauth2User = oauthToken.getPrincipal();

            if ("github".equalsIgnoreCase(registrationId)) {

                Object id = oauth2User.getAttribute("id");

                if (id == null) {
                    return Optional.empty();
                }

                return usuarioRepository.findByProvedorAndProvedorId(
                        ProvedorAutenticacao.GITHUB,
                        id.toString()
                );
            }

            if ("google".equalsIgnoreCase(registrationId)) {

                Object sub = oauth2User.getAttribute("sub");

                if (sub == null) {
                    return Optional.empty();
                }

                return usuarioRepository.findByProvedorAndProvedorId(
                        ProvedorAutenticacao.GOOGLE,
                        sub.toString()
                );
            }

            return Optional.empty();
        }

        return usuarioRepository.findByEmail(authentication.getName());
    }
}