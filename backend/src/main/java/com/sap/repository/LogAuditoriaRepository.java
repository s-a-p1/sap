package com.sap.repository;

import com.sap.model.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogAuditoriaRepository
        extends JpaRepository<LogAuditoria, Long> {

    List<LogAuditoria> findByUsuarioId(Long usuarioId);
}