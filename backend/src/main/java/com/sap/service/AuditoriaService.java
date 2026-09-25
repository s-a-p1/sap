package com.sap.service;

import com.sap.model.LogAuditoria;
import com.sap.repository.LogAuditoriaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;

    public AuditoriaService(
            LogAuditoriaRepository logAuditoriaRepository) {
        this.logAuditoriaRepository = logAuditoriaRepository;
    }

    public void registrar(
            Long usuarioId,
            String email,
            String acao,
            String descricao) {

        LogAuditoria log = new LogAuditoria(
                usuarioId,
                email,
                acao,
                descricao,
                LocalDateTime.now()
        );

        logAuditoriaRepository.save(log);
    }
}