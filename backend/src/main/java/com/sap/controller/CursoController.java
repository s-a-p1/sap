
package com.sap.controller;

import com.sap.model.Curso;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.CursoRepository;
import com.sap.repository.OfertaDisciplinaRepository;
import com.sap.repository.SemestreCursoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoRepository cursoRepository;
    private final SemestreCursoRepository semestreCursoRepository;
    private final OfertaDisciplinaRepository ofertaDisciplinaRepository;
    private final ConfiguracaoAcademicaRepository configuracaoAcademicaRepository;

    public CursoController(
            CursoRepository cursoRepository,
            SemestreCursoRepository semestreCursoRepository,
            OfertaDisciplinaRepository ofertaDisciplinaRepository,
            ConfiguracaoAcademicaRepository configuracaoAcademicaRepository) {
        this.cursoRepository = cursoRepository;
        this.semestreCursoRepository = semestreCursoRepository;
        this.ofertaDisciplinaRepository = ofertaDisciplinaRepository;
        this.configuracaoAcademicaRepository = configuracaoAcademicaRepository;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return cursoRepository.findAll()
                .stream()
                .map(curso -> Map.<String, Object>of(
                        "id", curso.getId(),
                        "nome", curso.getNome()
                ))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> cadastrar(@RequestBody Map<String, String> dados) {

        String nome = dados.get("nome");
        String descricao = dados.get("descricao");

        if (nome == null || nome.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("erro", "Informe o nome do curso.")
            );
        }

        nome = nome.trim();
        descricao = descricao == null ? "" : descricao.trim();

        if (nome.length() > 150 || descricao.length() > 255) {
            return ResponseEntity.badRequest().body(
                    Map.of("erro", "Nome ou descrição excede o limite permitido.")
            );
        }

        String nomeFinal = nome;

        boolean nomeExistente = cursoRepository.findAll()
                .stream()
                .anyMatch(curso ->
                        curso.getNome().equalsIgnoreCase(nomeFinal)
                );

        if (nomeExistente) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("erro", "Já existe um curso cadastrado com este nome.")
            );
        }

        Curso curso = new Curso(nome, descricao);

        try {
            Curso salvo = cursoRepository.saveAndFlush(curso);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "id", salvo.getId(),
                            "nome", salvo.getNome(),
                            "descricao", salvo.getDescricao()
                    )
            );
        } catch (DataIntegrityViolationException erro) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("erro", "Não foi possível cadastrar o curso. Verifique se o nome já existe.")
            );
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {

        Curso curso = cursoRepository.findById(id).orElse(null);

        if (curso == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("erro", "Curso não encontrado.")
            );
        }

        if (semestreCursoRepository.existsByCurso_Id(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("erro", "Não é possível excluir este curso porque ele possui semestres cadastrados.")
            );
        }

        if (ofertaDisciplinaRepository.existsByCurso_Id(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("erro", "Não é possível excluir este curso porque ele possui disciplinas ofertadas.")
            );
        }

        if (configuracaoAcademicaRepository.existsByCurso_Id(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("erro", "Não é possível excluir este curso porque existem estudantes vinculados.")
            );
        }

        try {
            cursoRepository.delete(curso);
            cursoRepository.flush();

            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException erro) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("erro", "Não é possível excluir este curso porque existem registros vinculados.")
            );
        }
    }
}
