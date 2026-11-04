
package com.sap.controller;

import com.sap.model.AnaliseDesempenho;
import com.sap.service.AnaliseDesempenhoService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

        try {
            return service.analisar(acertos, totalQuestoes);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }
}

