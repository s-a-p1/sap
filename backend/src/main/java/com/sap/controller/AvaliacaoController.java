
package com.sap.controller;

import com.sap.model.Assunto;
import com.sap.model.Avaliacao;
import com.sap.dto.AvaliacaoRequest;
import jakarta.validation.Valid;
import com.sap.model.Usuario;
import com.sap.repository.AssuntoRepository;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.QuestaoRepository;
import com.sap.repository.ResultadoRepository;
import com.sap.service.AvaliacaoService;
import com.sap.service.UsuarioAutenticadoService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final QuestaoRepository questaoRepository;
    private final AssuntoRepository assuntoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final ResultadoRepository resultadoRepository;

    public AvaliacaoController(
            AvaliacaoService avaliacaoService,
            UsuarioAutenticadoService usuarioAutenticadoService,
            QuestaoRepository questaoRepository,
            AssuntoRepository assuntoRepository,
            AvaliacaoRepository avaliacaoRepository,
            ResultadoRepository resultadoRepository) {

        this.avaliacaoService = avaliacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.questaoRepository = questaoRepository;
        this.assuntoRepository = assuntoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.resultadoRepository = resultadoRepository;
    }

    @PostMapping("/professor")
        public ResponseEntity<?> cadastrarProfessor(
        @Valid @RequestBody AvaliacaoRequest dados,
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

        String titulo = dados.getTitulo().trim();
        Long assuntoId = dados.getAssuntoId();

        Assunto assunto = assuntoRepository.findById(assuntoId).orElse(null);

        if (assunto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Assunto não encontrado."));
        }

        if (avaliacaoRepository.existsByAssunto_IdAndTituloIgnoreCase(
                assuntoId, titulo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Já existe uma avaliação com este título neste assunto."
                    ));
        }

        Avaliacao avaliacao = new Avaliacao(titulo, assunto);

        try {
            Avaliacao salva = avaliacaoRepository.saveAndFlush(avaliacao);

            Map<String, Object> resposta = new HashMap<>();

            resposta.put("id", salva.getId());
            resposta.put("titulo", salva.getTitulo());
            resposta.put("assuntoId", assunto.getId());
            resposta.put("disciplinaId", assunto.getDisciplina().getId());
            resposta.put("disciplinaNome", assunto.getDisciplina().getNome());
            resposta.put("quantidadeQuestoes", 0);
            resposta.put("dataCriacao", salva.getDataCriacao());

            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);

        } catch (DataIntegrityViolationException erro) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Não foi possível cadastrar a avaliação."
                    ));
        }
    }

    @PutMapping("/professor/{id}")
        public ResponseEntity<?> editarProfessor(
        @PathVariable Long id,
        @Valid @RequestBody AvaliacaoRequest dados,
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

        Avaliacao avaliacao = avaliacaoRepository.findById(id).orElse(null);

        if (avaliacao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Avaliação não encontrada."));
        }

        String titulo = dados.getTitulo().trim();
        Long assuntoId = dados.getAssuntoId();
        Assunto assunto = assuntoRepository.findById(assuntoId).orElse(null);

        if (assunto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Assunto não encontrado."));
        }

        boolean alterandoAssunto =
                !avaliacao.getAssunto().getId().equals(assuntoId);

        long quantidadeQuestoes =
                questaoRepository.countByAvaliacao_Id(id);

        if (alterandoAssunto && quantidadeQuestoes > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Não é possível alterar o assunto de uma avaliação que possui questões cadastradas."
                    ));
        }

        if (avaliacaoRepository.existsByAssunto_IdAndTituloIgnoreCaseAndIdNot(
                assuntoId, titulo, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Já existe outra avaliação com este título neste assunto."
                    ));
        }

        avaliacao.setTitulo(titulo);
        avaliacao.setAssunto(assunto);

        try {
            Avaliacao salva = avaliacaoRepository.saveAndFlush(avaliacao);

            Map<String, Object> resposta = new HashMap<>();

            resposta.put("id", salva.getId());
            resposta.put("titulo", salva.getTitulo());
            resposta.put("assuntoId", assunto.getId());
            resposta.put("disciplinaId", assunto.getDisciplina().getId());
            resposta.put("disciplinaNome", assunto.getDisciplina().getNome());
            resposta.put("quantidadeQuestoes", quantidadeQuestoes);
            resposta.put("dataCriacao", salva.getDataCriacao());

            return ResponseEntity.ok(resposta);

        } catch (DataIntegrityViolationException erro) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Não foi possível atualizar a avaliação."
                    ));
        }
    }

    @DeleteMapping("/professor/{id}")
    public ResponseEntity<?> excluirProfessor(
            @PathVariable Long id,
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

        Avaliacao avaliacao = avaliacaoRepository.findById(id).orElse(null);

        if (avaliacao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Avaliação não encontrada."));
        }

        if (questaoRepository.countByAvaliacao_Id(id) > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Não é possível excluir uma avaliação que possui questões cadastradas."
                    ));
        }

        if (resultadoRepository.existsByAvaliacao_Id(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Não é possível excluir uma avaliação que possui resultados de estudantes."
                    ));
        }

        try {
            avaliacaoRepository.delete(avaliacao);
            avaliacaoRepository.flush();

            return ResponseEntity.ok(
                    Map.of("mensagem", "Avaliação excluída com sucesso.")
            );

        } catch (DataIntegrityViolationException erro) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro",
                            "Não foi possível excluir a avaliação porque existem registros vinculados."
                    ));
        }
    }

    @GetMapping("/professor")
    public ResponseEntity<?> listarProfessor(Authentication authentication) {

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

        List<Map<String, Object>> avaliacoes =
                avaliacaoService.listarTodas().stream()
                        .map(avaliacao -> {
                            Map<String, Object> dados = new HashMap<>();

                            dados.put("id", avaliacao.getId());
                            dados.put("titulo", avaliacao.getTitulo());
                            dados.put("assuntoId", avaliacao.getAssunto().getId());
                            dados.put(
                                    "disciplinaId",
                                    avaliacao.getAssunto().getDisciplina().getId()
                            );
                            dados.put(
                                    "disciplinaNome",
                                    avaliacao.getAssunto().getDisciplina().getNome()
                            );
                            dados.put(
                                    "quantidadeQuestoes",
                                    questaoRepository.countByAvaliacao_Id(
                                            avaliacao.getId()
                                    )
                            );
                            dados.put("dataCriacao", avaliacao.getDataCriacao());

                            return dados;
                        })
                        .toList();

        return ResponseEntity.ok(avaliacoes);
    }

    @GetMapping
    public ResponseEntity<?> listar(Authentication authentication) {

        Optional<Usuario> usuarioEncontrado =
                usuarioAutenticadoService.buscar(authentication);

        if (usuarioEncontrado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        Usuario usuario = usuarioEncontrado.get();

        if (!"ESTUDANTE".equalsIgnoreCase(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Acesso permitido apenas a estudantes."));
        }

        List<Map<String, Object>> avaliacoes =
                avaliacaoService.listarDoEstudante(usuario).stream()
                        .map(avaliacao -> Map.<String, Object>of(
                                "id", avaliacao.getId(),
                                "titulo", avaliacao.getTitulo(),
                                "disciplinaId",
                                avaliacao.getAssunto().getDisciplina().getId(),
                                "disciplinaNome",
                                avaliacao.getAssunto().getDisciplina().getNome()
                        ))
                        .toList();

        return ResponseEntity.ok(avaliacoes);
    }
}
