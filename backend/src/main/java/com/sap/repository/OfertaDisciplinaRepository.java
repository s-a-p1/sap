package com.sap.repository;

import com.sap.model.OfertaDisciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfertaDisciplinaRepository extends JpaRepository<OfertaDisciplina, Long> {

    List<OfertaDisciplina> findByCursoIdAndSemestre(Long cursoId, Integer semestre);

    boolean existsByDisciplina_Id(Long disciplinaId);

    boolean existsByCurso_IdAndSemestre(Long cursoId, Integer semestre);

    boolean existsByCurso_Id(Long cursoId);
}