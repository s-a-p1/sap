
package com.sap.controller;

import com.sap.model.Notificacao;
import com.sap.model.Usuario;
import com.sap.service.GeradorNotificacoesService;
import com.sap.service.NotificacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final GeradorNotificacoesService geradorNotificacoesService;

    public NotificacaoController(
            NotificacaoService notificacaoService,
            GeradorNotificacoesService geradorNotificacoesService) {
        this.notificacaoService = notificacaoService;
        this.geradorNotificacoesService = geradorNotificacoesService;
    }

    @GetMapping
    public ResponseEntity<?> listar(Authentication authentication) {
        Optional<Usuario> usuario =
                notificacaoService.buscarUsuarioAutenticado(authentication);

        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Long usuarioId = usuario.get().getId();

        geradorNotificacoesService.gerarLembretesMetas(usuarioId);
        geradorNotificacoesService.gerarRecomendacoesRevisao(usuarioId);


        List<Map<String, Object>> notificacoes =
                notificacaoService.listar(usuarioId)
                        .stream()
                        .map(this::converterResposta)
                        .toList();

        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<?> contarNaoLidas(Authentication authentication) {
        Optional<Usuario> usuario =
                notificacaoService.buscarUsuarioAutenticado(authentication);

        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Long usuarioId = usuario.get().getId();

        geradorNotificacoesService.gerarLembretesMetas(usuarioId);
        geradorNotificacoesService.gerarRecomendacoesRevisao(usuarioId);

        long quantidade = notificacaoService.contarNaoLidas(usuarioId);

        return ResponseEntity.ok(Map.of("quantidade", quantidade));
    }

    @PatchMapping("/{id}/ler")
    public ResponseEntity<?> marcarComoLida(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<Usuario> usuario =
                notificacaoService.buscarUsuarioAutenticado(authentication);

        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        boolean atualizado = notificacaoService
                .marcarComoLida(id, usuario.get().getId());

        if (!atualizado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Notificação não encontrada."));
        }

        return ResponseEntity.ok(
                Map.of("mensagem", "Notificação marcada como lida.")
        );
    }

    private Map<String, Object> converterResposta(Notificacao notificacao) {
        Map<String, Object> resposta = new java.util.HashMap<>();

        resposta.put("id", notificacao.getId());
        resposta.put("titulo", notificacao.getTitulo());
        resposta.put("mensagem", notificacao.getMensagem());
        resposta.put("categoria", notificacao.getCategoria());
        resposta.put("lida", notificacao.isLida());
        resposta.put("dataCriacao", notificacao.getDataCriacao());
        resposta.put("referenciaTipo", notificacao.getReferenciaTipo());
        resposta.put("referenciaId", notificacao.getReferenciaId());

        return resposta;
    }
}
