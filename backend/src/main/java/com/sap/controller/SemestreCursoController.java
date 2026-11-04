
package com.sap.controller;

import com.sap.model.Curso;
import com.sap.model.SemestreCurso;
import com.sap.repository.CursoRepository;
import com.sap.repository.SemestreCursoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.OfertaDisciplinaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/semestres-cursos")
public class SemestreCursoController {

    private final SemestreCursoRepository semestreCursoRepository;
    private final CursoRepository cursoRepository;
    private final OfertaDisciplinaRepository ofertaDisciplinaRepository;
    private final ConfiguracaoAcademicaRepository configuracaoAcademicaRepository;

    public SemestreCursoController(
        SemestreCursoRepository semestreCursoRepository,
        CursoRepository cursoRepository,
        OfertaDisciplinaRepository ofertaDisciplinaRepository,
        ConfiguracaoAcademicaRepository configuracaoAcademicaRepository) {
    this.semestreCursoRepository = semestreCursoRepository;
    this.cursoRepository = cursoRepository;
    this.ofertaDisciplinaRepository = ofertaDisciplinaRepository;
    this.configuracaoAcademicaRepository = configuracaoAcademicaRepository;
}
    @GetMapping("/curso/{cursoId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<Map<String, Object>> listarPorCurso(
            @PathVariable Long cursoId) {

        return semestreCursoRepository
                .findByCurso_IdOrderByNumeroAsc(cursoId)
                .stream()
                .map(semestre -> Map.<String, Object>of(
                        "id", semestre.getId(),
                        "cursoId", semestre.getCurso().getId(),
                        "numero", semestre.getNumero()
                ))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> cadastrar(
            @RequestBody Map<String, Long> dados) {

        Long cursoId = dados.get("cursoId");
        Long numero = dados.get("numero");

        if (cursoId == null || numero == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("erro", "Informe o curso e o número do semestre.")
            );
        }

        if (numero < 1 || numero > 20) {
            return ResponseEntity.badRequest().body(
                    Map.of("erro", "O semestre deve estar entre 1 e 20.")
            );
        }

        Curso curso = cursoRepository.findById(cursoId).orElse(null);

        if (curso == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("erro", "Curso não encontrado.")
            );
        }

        if (semestreCursoRepository.existsByCurso_IdAndNumero(
                cursoId, numero.intValue())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("erro", "Este semestre já está cadastrado no curso.")
            );
        }

        SemestreCurso semestre = new SemestreCurso(
                curso,
                numero.intValue()
        );

        SemestreCurso salvo = semestreCursoRepository.save(semestre);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "id", salvo.getId(),
                        "cursoId", cursoId,
                        "numero", salvo.getNumero()
                )
        );
    }

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('PROFESSOR')")
@Transactional
public ResponseEntity<?> excluir(@PathVariable Long id) {

    SemestreCurso semestre = semestreCursoRepository.findById(id)
            .orElse(null);

    if (semestre == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("erro", "Semestre não encontrado.")
        );
    }

    Long cursoId = semestre.getCurso().getId();
    Integer numero = semestre.getNumero();

    if (ofertaDisciplinaRepository.existsByCurso_IdAndSemestre(
            cursoId, numero)) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "erro",
                        "Não é possível excluir este semestre porque ele possui disciplinas ofertadas."
                )
        );
    }

    if (configuracaoAcademicaRepository.existsByCurso_IdAndSemestre(
            cursoId, numero)) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "erro",
                        "Não é possível excluir este semestre porque existem estudantes vinculados."
                )
        );
    }

    semestreCursoRepository.delete(semestre);

    return ResponseEntity.noContent().build();
}

}
