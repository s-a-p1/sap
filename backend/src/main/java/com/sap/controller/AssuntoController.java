package com.sap.controller;

import com.sap.model.Assunto;
import com.sap.service.AssuntoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}