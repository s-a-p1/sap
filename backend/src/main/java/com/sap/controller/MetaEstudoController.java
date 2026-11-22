
package com.sap.controller;

import com.sap.model.MetaEstudo;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import com.sap.service.MetaEstudoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/plano-estudos")
public class MetaEstudoController {

    private final MetaEstudoService metaEstudoService;
    private final UsuarioRepository usuarioRepository;

    public MetaEstudoController(
            MetaEstudoService metaEstudoService,
            UsuarioRepository usuarioRepository) {
        this.metaEstudoService = metaEstudoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<?> listarMetas(Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                buscarUsuarioAutenticado(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"ESTUDANTE".equalsIgnoreCase(String.valueOf(usuario.getTipo()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a estudantes."));
        }

        metaEstudoService.gerarMetasAutomaticamente(usuario.getId());

        List<MetaEstudo> metas =
                metaEstudoService.listarMetas(usuario.getId());

        List<Map<String, Object>> resposta = metas.stream()
                .map(meta -> Map.<String, Object>of(
                        "id", meta.getId(),
                        "titulo", meta.getTitulo(),
                        "prioridade", meta.getPrioridade(),
                        "dataPrevista", meta.getDataPrevista(),
                        "concluida", meta.getConcluida(),
                        "assuntoId", meta.getAssunto().getId(),
                        "assuntoNome", meta.getAssunto().getNome(),
                        "disciplinaId", meta.getAssunto().getDisciplina().getId(),
                        "disciplinaNome", meta.getAssunto().getDisciplina().getNome()
                ))
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/gerar")
    public ResponseEntity<?> gerarMetas(Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                buscarUsuarioAutenticado(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"ESTUDANTE".equalsIgnoreCase(String.valueOf(usuario.getTipo()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a estudantes."));
        }

        metaEstudoService.gerarMetasAutomaticamente(usuario.getId());

        return ResponseEntity.ok(
                Map.of("mensagem", "Metas geradas com sucesso.")
        );
    }

    @PatchMapping("/{id}/conclusao")
    public ResponseEntity<?> atualizarConclusao(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> dados,
            Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                buscarUsuarioAutenticado(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"ESTUDANTE".equalsIgnoreCase(String.valueOf(usuario.getTipo()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a estudantes."));
        }

        Boolean concluida = dados.get("concluida");

        if (concluida == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Informe o estado de conclusão da meta."));
        }

        try {
            MetaEstudo meta = metaEstudoService.atualizarConclusao(
                    id,
                    usuario.getId(),
                    concluida
            );

            return ResponseEntity.ok(Map.of(
                    "id", meta.getId(),
                    "concluida", meta.getConcluida()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    private Optional<Usuario> buscarUsuarioAutenticado(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        return usuarioRepository.findByEmail(authentication.getName());
    }
}
