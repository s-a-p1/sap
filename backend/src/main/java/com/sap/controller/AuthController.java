package com.sap.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import com.sap.model.ProvedorAutenticacao;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.sap.dto.CadastroUsuarioRequest;
import com.sap.dto.LoginRequest;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import com.sap.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;

    public AuthController(
            UsuarioService usuarioService,
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository) {

        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody CadastroUsuarioRequest request) {

        try {

            Usuario usuario = usuarioService.cadastrar(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "id", usuario.getId(),
                            "nome", usuario.getNome(),
                            "email", usuario.getEmail(),
                            "tipo", usuario.getTipo(),
                            "provedor", usuario.getProvedor().name()
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        try {

            String emailNormalizado =
                    request.getEmail().trim().toLowerCase();

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    emailNormalizado,
                                    request.getSenha()
                            )
                    );

            SecurityContext securityContext =
                    SecurityContextHolder.createEmptyContext();

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            httpRequest.getSession(true)
                    .setAttribute(
                            "SPRING_SECURITY_CONTEXT",
                            securityContext
                    );

            return usuarioRepository.findByEmail(emailNormalizado)
                    .<ResponseEntity<?>>map(usuario ->
                            ResponseEntity.ok(
                                    Map.of(
                                            "mensagem", "Login realizado com sucesso.",
                                            "id", usuario.getId(),
                                            "nome", usuario.getNome(),
                                            "email", usuario.getEmail(),
                                            "tipo", usuario.getTipo(),
                                            "provedor", usuario.getProvedor().name()
                                    )
                            )
                    )
                    .orElseGet(() ->
                            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                    .body(Map.of(
                                            "erro", "Usuário não encontrado."
                                    ))
                    );

        } catch (AuthenticationException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "erro", "E-mail ou senha inválidos."
                    ));
        }
    }

    @GetMapping("/me")
        public ResponseEntity<?> usuarioAtual(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "erro", "Usuário não autenticado."
                        ));
        }

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {

                String registrationId =
                        oauthToken.getAuthorizedClientRegistrationId();

                OAuth2User oauth2User = oauthToken.getPrincipal();

                ProvedorAutenticacao provedor;
                String provedorId;

                if ("github".equalsIgnoreCase(registrationId)) {

                Object idAttribute = oauth2User.getAttribute("id");

                if (idAttribute == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of(
                                        "erro",
                                        "Não foi possível identificar o usuário GitHub."
                                ));
                }

                provedor = ProvedorAutenticacao.GITHUB;
                provedorId = idAttribute.toString();

                } else if ("google".equalsIgnoreCase(registrationId)) {

                Object subAttribute = oauth2User.getAttribute("sub");

                if (subAttribute == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of(
                                        "erro",
                                        "Não foi possível identificar o usuário Google."
                                ));
                }

                provedor = ProvedorAutenticacao.GOOGLE;
                provedorId = subAttribute.toString();

                } else {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "erro",
                                "Provedor OAuth2 não reconhecido."
                        ));
                }

                return usuarioRepository
                        .findByProvedorAndProvedorId(
                                provedor,
                                provedorId
                        )
                        .<ResponseEntity<?>>map(usuario ->
                                ResponseEntity.ok(
                                        Map.of(
                                                "id", usuario.getId(),
                                                "nome", usuario.getNome(),
                                                "email", usuario.getEmail(),
                                                "tipo", usuario.getTipo(),
                                                "provedor", usuario.getProvedor().name()
                                        )
                                )
                        )
                        .orElseGet(() ->
                                ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of(
                                                "erro",
                                                "Usuário OAuth2 não encontrado."
                                        ))
                        );
        }

        String email = authentication.getName();

        return usuarioRepository.findByEmail(email)
                .<ResponseEntity<?>>map(usuario ->
                        ResponseEntity.ok(
                                Map.of(
                                        "id", usuario.getId(),
                                        "nome", usuario.getNome(),
                                        "email", usuario.getEmail(),
                                        "tipo", usuario.getTipo(),
                                        "provedor", usuario.getProvedor().name()
                                )
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                        "erro",
                                        "Usuário não encontrado."
                                ))
                );
        }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                Map.of("mensagem", "Logout realizado com sucesso.")
        );
    }

}