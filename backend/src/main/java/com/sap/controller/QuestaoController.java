
package com.sap.controller;

import com.sap.dto.QuestaoResponse;
import com.sap.model.Avaliacao;
import com.sap.model.Questao;
import com.sap.model.Usuario;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.QuestaoRepository;
import com.sap.repository.ResultadoRepository;
import com.sap.service.QuestaoService;
import com.sap.service.UsuarioAutenticadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/questoes")
@CrossOrigin(origins = "http://localhost:5173")
public class QuestaoController {

    private final QuestaoService questaoService;
    private final QuestaoRepository questaoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ResultadoRepository resultadoRepository;

    public QuestaoController(
            QuestaoService questaoService,
            QuestaoRepository questaoRepository,
            ResultadoRepository resultadoRepository,
            AvaliacaoRepository avaliacaoRepository,
            UsuarioAutenticadoService usuarioAutenticadoService) {

        this.questaoService = questaoService;
        this.questaoRepository = questaoRepository;
        this.resultadoRepository = resultadoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping("/avaliacao/{avaliacaoId}")
    public List<QuestaoResponse> listarPorAvaliacao(
            @PathVariable Long avaliacaoId) {

        return questaoService.listarPorAvaliacao(avaliacaoId);
    }

    @GetMapping("/professor/avaliacao/{avaliacaoId}")
    public ResponseEntity<?> listarProfessor(
            @PathVariable Long avaliacaoId,
            Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                usuarioAutenticadoService.buscar(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"PROFESSOR".equalsIgnoreCase(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a professores."));
        }

        Avaliacao avaliacao =
                avaliacaoRepository.findById(avaliacaoId).orElse(null);

        if (avaliacao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Avaliação não encontrada."));
        }

        List<Map<String, Object>> questoes =
                questaoRepository.findByAvaliacao_Id(avaliacaoId)
                        .stream()
                        .map(questao -> Map.<String, Object>of(
                                "id", questao.getId(),
                                "enunciado", questao.getEnunciado(),
                                "alternativaA", questao.getAlternativaA(),
                                "alternativaB", questao.getAlternativaB(),
                                "alternativaC", questao.getAlternativaC(),
                                "alternativaD", questao.getAlternativaD(),
                                "respostaCorreta", questao.getRespostaCorreta()
                        ))
                        .toList();

        return ResponseEntity.ok(questoes);
    }

    @PostMapping("/professor")
    public ResponseEntity<?> cadastrarQuestaoProfessor(
            @RequestBody Map<String, Object> dados,
            Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                usuarioAutenticadoService.buscar(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"PROFESSOR".equalsIgnoreCase(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a professores."));
        }

        Long avaliacaoId;

        try {
            avaliacaoId = Long.valueOf(
                    String.valueOf(dados.get("avaliacaoId"))
            );
        } catch (NumberFormatException excecao) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Informe uma avaliação válida."));
        }

        Avaliacao avaliacao =
                avaliacaoRepository.findById(avaliacaoId).orElse(null);

        if (avaliacao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Avaliação não encontrada."));
        }

        if (resultadoRepository.existsByAvaliacao_Id(avaliacaoId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Não é possível adicionar questões porque esta avaliação já possui resultados de estudantes."
                    ));
        }

        String enunciado = dados.get("enunciado") instanceof String valor
                ? valor.trim() : "";

        String alternativaA = dados.get("alternativaA") instanceof String valor
                ? valor.trim() : "";

        String alternativaB = dados.get("alternativaB") instanceof String valor
                ? valor.trim() : "";

        String alternativaC = dados.get("alternativaC") instanceof String valor
                ? valor.trim() : "";

        String alternativaD = dados.get("alternativaD") instanceof String valor
                ? valor.trim() : "";

        String respostaCorreta = dados.get("respostaCorreta") instanceof String valor
                ? valor.trim().toUpperCase() : "";

        if (enunciado.isBlank()
                || alternativaA.isBlank()
                || alternativaB.isBlank()
                || alternativaC.isBlank()
                || alternativaD.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Preencha todos os campos da questão."));
        }

        if (alternativaA.length() > 255
                || alternativaB.length() > 255
                || alternativaC.length() > 255
                || alternativaD.length() > 255) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "erro",
                            "Cada alternativa deve ter no máximo 255 caracteres."
                    ));
        }

        if (!Set.of("A", "B", "C", "D").contains(respostaCorreta)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Selecione uma resposta correta válida."));
        }

        Questao questao = new Questao(
                enunciado,
                alternativaA,
                alternativaB,
                alternativaC,
                alternativaD,
                respostaCorreta,
                avaliacao
        );

        questao.setAssunto(avaliacao.getAssunto());

        Questao questaoSalva = questaoRepository.save(questao);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "id", questaoSalva.getId(),
                        "mensagem", "Questão cadastrada com sucesso."
                ));
    }

    @PutMapping("/professor/{questaoId}")
    public ResponseEntity<?> editarQuestaoProfessor(
            @PathVariable Long questaoId,
            @RequestBody Map<String, Object> dados,
            Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                usuarioAutenticadoService.buscar(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"PROFESSOR".equalsIgnoreCase(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a professores."));
        }

        Questao questao = questaoRepository.findById(questaoId).orElse(null);

        if (questao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Questão não encontrada."));
        }

        Long avaliacaoId = questao.getAvaliacao().getId();

        if (resultadoRepository.existsByAvaliacao_Id(avaliacaoId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Esta questão não pode ser editada porque a avaliação já possui resultados de estudantes."
                    ));
        }

        String enunciado = dados.get("enunciado") instanceof String valor
                ? valor.trim() : "";

        String alternativaA = dados.get("alternativaA") instanceof String valor
                ? valor.trim() : "";

        String alternativaB = dados.get("alternativaB") instanceof String valor
                ? valor.trim() : "";

        String alternativaC = dados.get("alternativaC") instanceof String valor
                ? valor.trim() : "";

        String alternativaD = dados.get("alternativaD") instanceof String valor
                ? valor.trim() : "";

        String respostaCorreta = dados.get("respostaCorreta") instanceof String valor
                ? valor.trim().toUpperCase() : "";

        if (enunciado.isBlank()
                || alternativaA.isBlank()
                || alternativaB.isBlank()
                || alternativaC.isBlank()
                || alternativaD.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Preencha todos os campos da questão."));
        }

        if (alternativaA.length() > 255
                || alternativaB.length() > 255
                || alternativaC.length() > 255
                || alternativaD.length() > 255) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "erro",
                            "Cada alternativa deve ter no máximo 255 caracteres."
                    ));
        }

        if (!Set.of("A", "B", "C", "D").contains(respostaCorreta)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Selecione uma resposta correta válida."));
        }

        questao.setEnunciado(enunciado);
        questao.setAlternativaA(alternativaA);
        questao.setAlternativaB(alternativaB);
        questao.setAlternativaC(alternativaC);
        questao.setAlternativaD(alternativaD);
        questao.setRespostaCorreta(respostaCorreta);

        questaoRepository.save(questao);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Questão atualizada com sucesso."
        ));
    }

    @DeleteMapping("/professor/{questaoId}")
    public ResponseEntity<?> excluirQuestaoProfessor(
            @PathVariable Long questaoId,
            Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                usuarioAutenticadoService.buscar(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"PROFESSOR".equalsIgnoreCase(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a professores."));
        }

        Questao questao = questaoRepository.findById(questaoId).orElse(null);

        if (questao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Questão não encontrada."));
        }

        Long avaliacaoId = questao.getAvaliacao().getId();

        if (resultadoRepository.existsByAvaliacao_Id(avaliacaoId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Esta questão não pode ser excluída porque a avaliação já possui resultados de estudantes."
                    ));
        }

        questaoRepository.delete(questao);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Questão excluída com sucesso."
        ));
    }
}
