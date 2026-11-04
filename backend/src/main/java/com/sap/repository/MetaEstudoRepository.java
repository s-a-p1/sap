
package com.sap.repository;

import com.sap.model.MetaEstudo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaEstudoRepository extends JpaRepository<MetaEstudo, Long> {

    List<MetaEstudo> findByUsuarioIdOrderByDataPrevistaAsc(Long usuarioId);

    List<MetaEstudo> findByUsuarioIdAndAssuntoIdInOrderByDataPrevistaAsc(
            Long usuarioId,
            List<Long> assuntoIds
    );

    Optional<MetaEstudo> findByUsuarioIdAndAssuntoIdAndConcluidaFalse(
            Long usuarioId,
            Long assuntoId
    );

    Optional<MetaEstudo> findFirstByUsuarioIdAndAssuntoIdOrderByIdDesc(
            Long usuarioId,
            Long assuntoId
    );

    long countByUsuarioIdAndConcluidaTrue(Long usuarioId);

        List<MetaEstudo> findByUsuarioIdAndConcluidaFalseOrderByDataPrevistaAsc(
        Long usuarioId
        );

}
