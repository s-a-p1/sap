
package com.sap.service;

import com.sap.model.TokenRecuperacaoSenha;
import com.sap.model.Usuario;
import com.sap.model.ProvedorAutenticacao;
import com.sap.repository.TokenRecuperacaoSenhaRepository;
import com.sap.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RecuperacaoSenhaService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final GeradorTokenRecuperacao geradorToken;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    private final String frontendUrl;

    public RecuperacaoSenhaService(
            UsuarioRepository usuarioRepository,
            TokenRecuperacaoSenhaRepository tokenRepository,
            GeradorTokenRecuperacao geradorToken,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            TransactionTemplate transactionTemplate,
            @Value("${app.frontend.url}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.geradorToken = geradorToken;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = transactionTemplate;
        this.frontendUrl = frontendUrl;
    }

    public void solicitarRecuperacao(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Optional<Usuario> usuarioEncontrado =
                usuarioRepository.findByEmail(email.trim());

        if (usuarioEncontrado.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!usuario.isAtivo()
                || usuario.getProvedor() != ProvedorAutenticacao.LOCAL) {
            return;
        }

        LocalDateTime agoraSolicitacao = LocalDateTime.now();

        boolean solicitacaoRecente =
                tokenRepository.existsByUsuarioIdAndDataCriacaoAfter(
                        usuario.getId(),
                        agoraSolicitacao.minusSeconds(60)
                );

        if (solicitacaoRecente) {
            return;
        }

        long solicitacoesUltimaHora =
                tokenRepository.countByUsuarioIdAndDataCriacaoAfter(
                        usuario.getId(),
                        agoraSolicitacao.minusHours(1)
                );

        if (solicitacoesUltimaHora >= 5) {
            return;
        }

        String token = geradorToken.gerarToken();
        String hash = geradorToken.gerarHash(token);

        transactionTemplate.executeWithoutResult(status -> {
            LocalDateTime agora = LocalDateTime.now();

            tokenRepository.findByUsuarioIdAndDataUtilizacaoIsNull(usuario.getId())
                    .forEach(anterior -> {
                        anterior.setDataUtilizacao(agora);
                        tokenRepository.save(anterior);
                    });

            TokenRecuperacaoSenha recuperacao = new TokenRecuperacaoSenha();
            recuperacao.setUsuario(usuario);
            recuperacao.setTokenHash(hash);
            recuperacao.setDataCriacao(agora);
            recuperacao.setDataExpiracao(agora.plusMinutes(15));

            tokenRepository.save(recuperacao);

        });

        String link = frontendUrl.replaceAll("/+$", "")
                + "/redefinir-senha?token=" + token;

        emailService.enviarRecuperacaoSenha(usuario.getEmail(), link);
    }

    public boolean redefinirSenha(String token, String novaSenha) {
        if (token == null || token.isBlank()
                || novaSenha == null || novaSenha.length() < 8
                || novaSenha.length() > 72) {
            return false;
        }

        String hash = geradorToken.gerarHash(token);

        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            Optional<TokenRecuperacaoSenha> encontrado =
        tokenRepository.findByTokenHashComBloqueio(hash);

            if (encontrado.isEmpty()) {
                return false;
            }

            TokenRecuperacaoSenha recuperacao = encontrado.get();
            LocalDateTime agora = LocalDateTime.now();

            if (recuperacao.getDataUtilizacao() != null
                    || !recuperacao.getDataExpiracao().isAfter(agora)) {
                return false;
            }

            Usuario usuario = recuperacao.getUsuario();

            if (!usuario.isAtivo()
                    || usuario.getProvedor() != ProvedorAutenticacao.LOCAL) {
                return false;
            }

            usuario.setSenha(passwordEncoder.encode(novaSenha));
            usuarioRepository.save(usuario);

            recuperacao.setDataUtilizacao(agora);
            tokenRepository.save(recuperacao);

            return true;
        }));
    }
}
