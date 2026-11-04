
package com.sap.controller;

import com.sap.dto.PreferenciaNotificacaoDTO;
import com.sap.model.Usuario;
import com.sap.service.NotificacaoService;
import com.sap.service.PreferenciaNotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/preferencias-notificacoes")
public class PreferenciaNotificacaoController {

    private final PreferenciaNotificacaoService preferenciaService;
    private final NotificacaoService notificacaoService;

    public PreferenciaNotificacaoController(
            PreferenciaNotificacaoService preferenciaService,
            NotificacaoService notificacaoService) {
        this.preferenciaService = preferenciaService;
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public ResponseEntity<?> buscar(Authentication authentication) {
        Usuario usuario = buscarUsuarioAutenticado(authentication);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        return ResponseEntity.ok(
                PreferenciaNotificacaoDTO.fromEntity(
                        preferenciaService.buscarOuCriar(usuario.getId())
                )
        );
    }

    @PutMapping
    public ResponseEntity<?> atualizar(
            Authentication authentication,
            @RequestBody PreferenciaNotificacaoDTO dados) {

        Usuario usuario = buscarUsuarioAutenticado(authentication);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        if (dados.antecedenciaLembretes() < 0
                || dados.antecedenciaLembretes() > 2) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "erro",
                            "A antecedência deve ser de 0, 1 ou 2 dias."
                    ));
        }

        return ResponseEntity.ok(
                PreferenciaNotificacaoDTO.fromEntity(
                        preferenciaService.salvar(
                                usuario.getId(),
                                dados.toEntity()
                        )
                )
        );
    }

    private Usuario buscarUsuarioAutenticado(
            Authentication authentication) {

        return notificacaoService
                .buscarUsuarioAutenticado(authentication)
                .orElse(null);
    }
}
