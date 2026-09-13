package com.sap.controller;

import com.sap.dto.FinalizarAvaliacaoRequest;
import com.sap.dto.ResultadoAvaliacaoResponse;
import com.sap.service.CorrecaoAvaliacaoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/avaliacoes")
@CrossOrigin(origins = "http://localhost:5173")
public class CorrecaoAvaliacaoController {

    private final CorrecaoAvaliacaoService correcaoAvaliacaoService;

    public CorrecaoAvaliacaoController(
            CorrecaoAvaliacaoService correcaoAvaliacaoService) {

        this.correcaoAvaliacaoService = correcaoAvaliacaoService;
    }

    @PostMapping("/finalizar")
    public ResultadoAvaliacaoResponse finalizar(
            @RequestBody FinalizarAvaliacaoRequest request) {

        return correcaoAvaliacaoService.corrigir(request);
    }
}