package com.sap.controller;

import com.sap.model.Disciplina;
import com.sap.service.DisciplinaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @GetMapping
    public List<Disciplina> listarTodas() {
        return disciplinaService.listarTodas();
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Disciplina> cadastrar(@RequestBody Disciplina disciplina) {
        Disciplina novaDisciplina = disciplinaService.cadastrar(disciplina);

        return ResponseEntity.status(HttpStatus.CREATED).body(novaDisciplina);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        disciplinaService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, String>> tratarDisciplinaNaoEncontrada(
        IllegalArgumentException erro) {

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("erro", erro.getMessage()));
}

@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<Map<String, String>> tratarDisciplinaComVinculos(
        IllegalStateException erro) {

    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("erro", erro.getMessage()));
}
}