package com.sap.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sap.model.AnaliseDesempenho;
import com.sap.service.AnaliseDesempenhoService;

@RestController
@RequestMapping("/api/analises")
public class AnaliseDesempenhoController {

    private final AnaliseDesempenhoService service;

    public AnaliseDesempenhoController(AnaliseDesempenhoService service) {
        this.service = service;
    }

    @PostMapping
    public AnaliseDesempenho analisar(
            @RequestParam Integer acertos,
            @RequestParam Integer totalQuestoes) {

        return service.analisar(acertos, totalQuestoes);
    }
}