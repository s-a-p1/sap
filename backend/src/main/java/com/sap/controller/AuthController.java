
package com.sap.controller;

import com.sap.dto.AceiteTermosRequest;
import com.sap.dto.AlterarSenhaRequest;
import com.sap.dto.AtualizarPerfilRequest;
import com.sap.dto.CadastroUsuarioRequest;
import com.sap.dto.ExcluirContaRequest;
import com.sap.dto.LoginRequest;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import com.sap.service.AuditoriaService;
import com.sap.service.CustomUserDetailsService;
import com.sap.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(
            UsuarioService usuarioService,
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService,
            CustomUserDetailsService customUserDetailsService) {

        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.customUserDetailsService = customUserDetailsService;
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
                            "provedor", usuario.getProvedor().name(),
                            "aceitouTermos", usuario.isAceitouTermos()
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
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

            httpRequest.getSession(true).setAttribute(
                    "SPRING_SECURITY_CONTEXT",
                    securityContext
            );

            Optional<Usuario> usuarioEncontrado =
                    usuarioRepository.findByEmail(emailNormalizado);

            if (usuarioEncontrado.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não encontrado."));
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
                            "provedor", usuario.getProvedor().name(),
                            "aceitouTermos", usuario.isAceitouTermos()
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
                    .body(Map.of("erro", "E-mail ou senha inválidos."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> usuarioAtual(
            Authentication authentication) {

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
                                        "provedor", valor.getProvedor().name(),
                                        "aceitouTermos", valor.isAceitouTermos()
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

    @PostMapping("/aceitar-termos")
    public ResponseEntity<?> aceitarTermos(
            @Valid @RequestBody AceiteTermosRequest request,
            Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                buscarUsuarioAutenticado(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        usuario.setAceitouTermos(true);
        usuario.setDataAceiteTermos(LocalDateTime.now());
        usuario.setVersaoTermos("1.0");

        usuarioRepository.save(usuario);

        auditoriaService.registrar(
                usuario.getId(),
                usuario.getEmail(),
                "ACEITE_TERMOS",
                "Usuário aceitou os Termos de Uso e a Política de Privacidade. Versão: 1.0."
        );

        return ResponseEntity.ok(
                Map.of(
                        "mensagem", "Termos aceitos com sucesso.",
                        "aceitouTermos", usuario.isAceitouTermos()
                )
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

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                Map.of("mensagem", "Logout realizado com sucesso.")
        );
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> atualizarPerfil(
            @RequestBody AtualizarPerfilRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        Optional<Usuario> usuarioEncontrado =
                buscarUsuarioAutenticado(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        try {
            Usuario usuario = usuarioService.atualizarPerfil(
                    usuarioEncontrado.get().getId(),
                    request.nome(),
                    request.email()
            );

            UserDetails userDetails =
                    customUserDetailsService.loadUserByUsername(
                            usuario.getEmail()
                    );

            Authentication novaAutenticacao =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContext novoContexto =
                    SecurityContextHolder.createEmptyContext();

            novoContexto.setAuthentication(novaAutenticacao);
            SecurityContextHolder.setContext(novoContexto);

            httpRequest.getSession(true).setAttribute(
                    "SPRING_SECURITY_CONTEXT",
                    novoContexto
            );

            auditoriaService.registrar(
                    usuario.getId(),
                    usuario.getEmail(),
                    "ATUALIZACAO_PERFIL",
                    "Usuário atualizou seus dados de perfil."
            );

            return ResponseEntity.ok(
                    Map.of(
                            "id", usuario.getId(),
                            "nome", usuario.getNome(),
                            "email", usuario.getEmail(),
                            "tipo", usuario.getTipo()
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/alterar-senha")
    public ResponseEntity<?> alterarSenha(
            @RequestBody AlterarSenhaRequest request,
            Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                buscarUsuarioAutenticado(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        if (request.novaSenha() == null
                || !request.novaSenha().equals(request.confirmarSenha())) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "erro",
                            "A confirmação da senha não corresponde à nova senha."
                    ));
        }

        try {
            Usuario usuario = usuarioEncontrado.get();

            usuarioService.alterarSenha(
                    usuario.getId(),
                    request.senhaAtual(),
                    request.novaSenha()
            );

            auditoriaService.registrar(
                    usuario.getId(),
                    usuario.getEmail(),
                    "ALTERACAO_SENHA",
                    "Usuário alterou sua senha."
            );

            return ResponseEntity.ok(
                    Map.of("mensagem", "Senha alterada com sucesso.")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/conta")
    public ResponseEntity<?> excluirConta(
            @RequestBody ExcluirContaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        Optional<Usuario> usuarioEncontrado =
                buscarUsuarioAutenticado(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        try {
            Usuario usuario = usuarioEncontrado.get();

            usuarioService.excluirConta(
                    usuario.getId(),
                    request.senhaAtual()
            );

            HttpSession session = httpRequest.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(
                    Map.of("mensagem", "Conta excluída com sucesso.")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    private Optional<Usuario> buscarUsuarioAutenticado(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        return usuarioRepository.findByEmail(
                authentication.getName()
        );
    }
}
