package com.sap.service;

import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class GitHubOAuth2UserService extends DefaultOAuth2UserService {

    private final UsuarioRepository usuarioRepository;

    public GitHubOAuth2UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oauth2User = super.loadUser(userRequest);

        Object idAttribute = oauth2User.getAttribute("id");

        String provedorId =
                idAttribute != null ? idAttribute.toString() : null;

        String login = oauth2User.getAttribute("login");
        String nome = oauth2User.getAttribute("name");
        String email = oauth2User.getAttribute("email");

        if (provedorId == null || provedorId.isBlank()) {
            throw new OAuth2AuthenticationException(
                    "GitHub não retornou o identificador do usuário."
            );
        }

        if (nome == null || nome.isBlank()) {
            nome = login;
        }

        /*
         * O GitHub pode não devolver o e-mail no endpoint de perfil
         * quando o usuário mantém o endereço privado.
         *
         * Como solicitamos o escopo user:email, consultamos
         * /user/emails quando isso acontecer.
         */
        if (email == null || email.isBlank()) {
            email = buscarEmailGitHub(userRequest);
        }

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    "Não foi possível obter um e-mail válido da conta GitHub."
            );
        }

        String emailNormalizado = email.trim().toLowerCase();

        Usuario usuario = usuarioRepository
                .findByProvedorAndProvedorId(
                        ProvedorAutenticacao.GITHUB,
                        provedorId
                )
                .orElse(null);

        if (usuario == null) {

            if (usuarioRepository.existsByEmail(emailNormalizado)) {
                throw new OAuth2AuthenticationException(
                        "Já existe uma conta cadastrada com este e-mail."
                );
            }

            usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(emailNormalizado);
            usuario.setSenha(null);
            usuario.setTipo("ESTUDANTE");
            usuario.setProvedor(ProvedorAutenticacao.GITHUB);
            usuario.setProvedorId(provedorId);
            usuario.setAtivo(true);

            usuarioRepository.save(usuario);
        }

        return oauth2User;
    }

    private String buscarEmailGitHub(OAuth2UserRequest userRequest) {

        String accessToken =
                userRequest.getAccessToken().getTokenValue();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github+json");

        RequestEntity<Void> request = new RequestEntity<>(
                headers,
                HttpMethod.GET,
                URI.create("https://api.github.com/user/emails")
        );

        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(
                        request,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );

        List<Map<String, Object>> emails = response.getBody();

        if (emails == null || emails.isEmpty()) {
            return null;
        }

        // Primeiro procura um e-mail primário e verificado.
        for (Map<String, Object> item : emails) {

            Boolean primary = (Boolean) item.get("primary");
            Boolean verified = (Boolean) item.get("verified");
            Object email = item.get("email");

            if (Boolean.TRUE.equals(primary)
                    && Boolean.TRUE.equals(verified)
                    && email != null) {

                return email.toString();
            }
        }

        // Se não houver primário, aceita outro e-mail verificado.
        for (Map<String, Object> item : emails) {

            Boolean verified = (Boolean) item.get("verified");
            Object email = item.get("email");

            if (Boolean.TRUE.equals(verified) && email != null) {
                return email.toString();
            }
        }

        return null;
    }
}