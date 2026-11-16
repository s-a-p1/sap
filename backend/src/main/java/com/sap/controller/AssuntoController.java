
package com.sap.controller;

import com.sap.model.Assunto;
import com.sap.service.AssuntoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assuntos")
public class AssuntoController {

    private final AssuntoService assuntoService;

    public AssuntoController(AssuntoService assuntoService) {
        this.assuntoService = assuntoService;
    }

    @GetMapping
    public List<Assunto> listarTodos() {
        return assuntoService.listarTodos();
    }

    @GetMapping("/disciplina/{disciplinaId}")
    public List<Assunto> listarPorDisciplina(@PathVariable Long disciplinaId) {
        return assuntoService.listarPorDisciplina(disciplinaId);
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Assunto> cadastrar(@Valid @RequestBody Assunto assunto) {
        Assunto novoAssunto = assuntoService.cadastrar(assunto);

        return ResponseEntity.status(HttpStatus.CREATED).body(novoAssunto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        assuntoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarErroCadastro(
            IllegalArgumentException erro) {

        return ResponseEntity.badRequest()
                .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> tratarAssuntoComVinculos(
            IllegalStateException erro) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", erro.getMessage()));
    }
}
