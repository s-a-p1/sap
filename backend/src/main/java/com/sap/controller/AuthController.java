package com.sap.controller;

import com.sap.dto.CadastroUsuarioRequest;
import com.sap.dto.LoginRequest;
import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import com.sap.service.AuditoriaService;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public AuthController(
            UsuarioService usuarioService,
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService) {

        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody CadastroUsuarioRequest request) {

        try {
            Usuario usuario = usuarioService.cadastrar(request);

            auditoriaService.registrar(
                    usuario.getId(),
                    usuario.getEmail(),
                    "CADASTRO",
                    "Conta de usuário criada com sucesso."
            );

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

        String emailNormalizado =
                request.getEmail().trim().toLowerCase();

        try {
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

            Optional<Usuario> usuarioEncontrado =
                    usuarioRepository.findByEmail(emailNormalizado);

            if (usuarioEncontrado.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "erro",
                                "Usuário não encontrado."
                        ));
            }

            Usuario usuario = usuarioEncontrado.get();

            auditoriaService.registrar(
                    usuario.getId(),
                    usuario.getEmail(),
                    "LOGIN",
                    "Login local realizado com sucesso."
            );

            return ResponseEntity.ok(
                    Map.of(
                            "mensagem", "Login realizado com sucesso.",
                            "id", usuario.getId(),
                            "nome", usuario.getNome(),
                            "email", usuario.getEmail(),
                            "tipo", usuario.getTipo(),
                            "provedor", usuario.getProvedor().name()
                    )
            );

        } catch (AuthenticationException e) {
            Usuario usuario =
                    usuarioRepository.findByEmail(emailNormalizado)
                            .orElse(null);

            auditoriaService.registrar(
                    usuario != null ? usuario.getId() : null,
                    emailNormalizado,
                    "LOGIN_FALHOU",
                    "Tentativa de login local sem sucesso."
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "erro",
                            "E-mail ou senha inválidos."
                    ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> usuarioAtual(Authentication authentication) {

        Optional<Usuario> usuario =
                buscarUsuarioAutenticado(authentication);

        return usuario
                .<ResponseEntity<?>>map(valor ->
                        ResponseEntity.ok(
                                Map.of(
                                        "id", valor.getId(),
                                        "nome", valor.getNome(),
                                        "email", valor.getEmail(),
                                        "tipo", valor.getTipo(),
                                        "provedor", valor.getProvedor().name()
                                )
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of(
                                        "erro",
                                        "Usuário não autenticado."
                                ))
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            Authentication authentication) {

        Optional<Usuario> usuario =
                buscarUsuarioAutenticado(authentication);

        usuario.ifPresent(valor ->
                auditoriaService.registrar(
                        valor.getId(),
                        valor.getEmail(),
                        "LOGOUT",
                        "Logout realizado com sucesso."
                )
        );

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                Map.of("mensagem", "Logout realizado com sucesso.")
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

        return usuarioRepository.findByEmail(authentication.getName());
    }
}