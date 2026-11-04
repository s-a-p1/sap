
package com.sap.repository;

import com.sap.model.ConfiguracaoAcademica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoAcademicaRepository
        extends JpaRepository<ConfiguracaoAcademica, Long> {

    Optional<ConfiguracaoAcademica> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByCurso_Id(Long cursoId);

    boolean existsByCurso_IdAndSemestre(Long cursoId, Integer semestre);
}
