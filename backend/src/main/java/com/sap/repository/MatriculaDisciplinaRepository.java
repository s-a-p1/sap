package com.sap.repository;

import com.sap.model.MatriculaDisciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaDisciplinaRepository
        extends JpaRepository<MatriculaDisciplina, Long> {

    List<MatriculaDisciplina> findByConfiguracaoAcademicaId(Long configuracaoId);

    boolean existsByConfiguracaoAcademicaIdAndOfertaDisciplinaId(
            Long configuracaoId,
            Long ofertaDisciplinaId
    );

boolean existsByConfiguracaoAcademica_Usuario_IdAndOfertaDisciplina_Disciplina_IdAndOfertaDisciplina_Curso_IdAndOfertaDisciplina_Semestre(
        Long usuarioId,
        Long disciplinaId,
        Long cursoId,
        Integer semestre
);

}