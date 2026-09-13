package com.sap.controller;

import com.sap.dto.QuestaoResponse;
import com.sap.service.QuestaoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questoes")
@CrossOrigin(origins = "http://localhost:5173")
public class QuestaoController {

    private final QuestaoService questaoService;

    public QuestaoController(QuestaoService questaoService) {
        this.questaoService = questaoService;
    }

    @GetMapping("/avaliacao/{avaliacaoId}")
    public List<QuestaoResponse> listarPorAvaliacao(
            @PathVariable Long avaliacaoId) {

        return questaoService.listarPorAvaliacao(avaliacaoId);
    }
}
