
package com.sap.repository;

import com.sap.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByAssuntoDisciplinaId(Long disciplinaId);

    boolean existsByAssunto_Id(Long assuntoId);

    boolean existsByAssunto_IdAndTituloIgnoreCase(Long assuntoId, String titulo);

    boolean existsByAssunto_IdAndTituloIgnoreCaseAndIdNot(
            Long assuntoId,
            String titulo,
            Long avaliacaoId
    );
}
