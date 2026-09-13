package com.sap.service;

import com.sap.model.Assunto;
import com.sap.repository.AssuntoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssuntoService {

    private final AssuntoRepository assuntoRepository;

    public AssuntoService(AssuntoRepository assuntoRepository) {
        this.assuntoRepository = assuntoRepository;
    }

    public List<Assunto> listarTodos() {
        return assuntoRepository.findAll();
    }
}
