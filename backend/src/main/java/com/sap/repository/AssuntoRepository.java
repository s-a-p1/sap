package com.sap.repository;

import com.sap.model.Assunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssuntoRepository extends JpaRepository<Assunto, Long> {

    List<Assunto> findByDisciplina_Id(Long disciplinaId);

    boolean existsByDisciplina_Id(Long disciplinaId);

    @Query("""
        SELECT a
        FROM Assunto a
        WHERE a.disciplina.id = :disciplinaId
          AND a.visivelCatalogo = true
        ORDER BY a.nome
        """)
    List<Assunto> listarAssuntosEspecificos(
        @Param("disciplinaId") Long disciplinaId
    );
}