package com.sap.controller;

import com.sap.repository.OfertaDisciplinaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ofertas-disciplinas")
public class OfertaDisciplinaController {

    private final OfertaDisciplinaRepository ofertaDisciplinaRepository;

    public OfertaDisciplinaController(
            OfertaDisciplinaRepository ofertaDisciplinaRepository) {
        this.ofertaDisciplinaRepository = ofertaDisciplinaRepository;
    }

    @GetMapping
    public List<Map<String, Object>> listar(
            @RequestParam Long cursoId,
            @RequestParam Integer semestre) {

        return ofertaDisciplinaRepository
                .findByCursoIdAndSemestre(cursoId, semestre)
                .stream()
                .map(oferta -> Map.<String, Object>of(
                        "id", oferta.getId(),
                        "disciplinaId", oferta.getDisciplina().getId(),
                        "disciplinaNome", oferta.getDisciplina().getNome(),
                        "semestre", oferta.getSemestre()
                ))
                .toList();
    }
}