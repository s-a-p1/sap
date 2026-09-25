package com.sap.controller;

import com.sap.dto.LivroResponse;
import com.sap.service.LivroService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService livroService;

    public LivroController(
            LivroService livroService) {

        this.livroService = livroService;
    }

    @GetMapping
    public List<LivroResponse> buscar(
            @RequestParam String busca) {

        return livroService.buscarLivros(busca);
    }
}