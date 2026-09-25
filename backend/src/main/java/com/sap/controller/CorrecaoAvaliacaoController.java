package com.sap.controller;

import com.sap.dto.FinalizarAvaliacaoRequest;
import com.sap.dto.ResultadoAvaliacaoResponse;
import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import com.sap.service.CorrecaoAvaliacaoService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/avaliacoes")
@CrossOrigin(origins = "http://localhost:5173")
public class CorrecaoAvaliacaoController {

    private final CorrecaoAvaliacaoService correcaoAvaliacaoService;
    private final UsuarioRepository usuarioRepository;

    public CorrecaoAvaliacaoController(
            CorrecaoAvaliacaoService correcaoAvaliacaoService,
            UsuarioRepository usuarioRepository) {

        this.correcaoAvaliacaoService = correcaoAvaliacaoService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/finalizar")
    public ResultadoAvaliacaoResponse finalizar(
            @RequestBody FinalizarAvaliacaoRequest request,
            Authentication authentication) {

        Usuario usuario = buscarUsuarioAutenticado(authentication)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Usuário não autenticado."
                        )
                );

        return correcaoAvaliacaoService.corrigir(
                request,
                usuario
        );
    }

    private Optional<Usuario> buscarUsuarioAutenticado(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {

            String registrationId =
                    oauthToken.getAuthorizedClientRegistrationId();

            OAuth2User oauth2User = oauthToken.getPrincipal();

            if ("github".equalsIgnoreCase(registrationId)) {

                Object idAttribute = oauth2User.getAttribute("id");

                if (idAttribute == null) {
                    return Optional.empty();
                }

                return usuarioRepository.findByProvedorAndProvedorId(
                        ProvedorAutenticacao.GITHUB,
                        idAttribute.toString()
                );
            }

            if ("google".equalsIgnoreCase(registrationId)) {

                Object subAttribute = oauth2User.getAttribute("sub");

                if (subAttribute == null) {
                    return Optional.empty();
                }

                return usuarioRepository.findByProvedorAndProvedorId(
                        ProvedorAutenticacao.GOOGLE,
                        subAttribute.toString()
                );
            }

            return Optional.empty();
        }

        return usuarioRepository.findByEmail(
                authentication.getName().trim().toLowerCase()
        );
    }
}