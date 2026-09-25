package com.sap.repository;

import com.sap.model.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAuditoriaRepository
        extends JpaRepository<LogAuditoria, Long> {
}