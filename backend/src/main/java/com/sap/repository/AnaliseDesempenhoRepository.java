
package com.sap.repository;

import com.sap.model.AnaliseDesempenho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnaliseDesempenhoRepository
        extends JpaRepository<AnaliseDesempenho, Long> {

    Optional<AnaliseDesempenho> findByResultado_Id(Long resultadoId);
}
