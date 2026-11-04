
package com.sap.service;

import com.sap.model.Notificacao;
import com.sap.model.Usuario;
import com.sap.repository.NotificacaoRepository;
import com.sap.repository.UsuarioRepository;
import com.sap.model.ProvedorAutenticacao;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> buscarUsuarioAutenticado(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            OAuth2User oauth2User = oauthToken.getPrincipal();

            if ("github".equalsIgnoreCase(registrationId)) {
                Object id = oauth2User.getAttribute("id");

                return id == null
                        ? Optional.empty()
                        : usuarioRepository.findByProvedorAndProvedorId(
                                ProvedorAutenticacao.GITHUB,
                                id.toString()
                        );
            }

            if ("google".equalsIgnoreCase(registrationId)) {
                Object sub = oauth2User.getAttribute("sub");

                return sub == null
                        ? Optional.empty()
                        : usuarioRepository.findByProvedorAndProvedorId(
                                ProvedorAutenticacao.GOOGLE,
                                sub.toString()
                        );
            }

            return Optional.empty();
        }

        return usuarioRepository.findByEmail(authentication.getName());
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listar(Long usuarioId) {
        return notificacaoRepository
                .findByUsuarioIdOrderByDataCriacaoDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long usuarioId) {
        return notificacaoRepository
                .countByUsuarioIdAndLidaFalse(usuarioId);
    }

    @Transactional
    public boolean marcarComoLida(Long notificacaoId, Long usuarioId) {
        Optional<Notificacao> encontrada = notificacaoRepository
                .findByIdAndUsuarioId(notificacaoId, usuarioId);

        if (encontrada.isEmpty()) {
            return false;
        }

        Notificacao notificacao = encontrada.get();
        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);

        return true;
    }

    @Transactional
    public boolean atualizarLeitura(
            Long notificacaoId,
            Long usuarioId,
            boolean lida) {

        Optional<Notificacao> encontrada = notificacaoRepository
                .findByIdAndUsuarioId(notificacaoId, usuarioId);

        if (encontrada.isEmpty()) {
            return false;
        }

        Notificacao notificacao = encontrada.get();
        notificacao.setLida(lida);
        notificacaoRepository.save(notificacao);

        return true;
    }
}
