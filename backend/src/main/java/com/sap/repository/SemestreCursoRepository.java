
package com.sap.repository;

import com.sap.model.SemestreCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SemestreCursoRepository extends JpaRepository<SemestreCurso, Long> {

    List<SemestreCurso> findByCurso_IdOrderByNumeroAsc(Long cursoId);

    boolean existsByCurso_IdAndNumero(Long cursoId, Integer numero);

    boolean existsByCurso_Id(Long cursoId);
}
