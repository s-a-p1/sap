
package com.sap.repository;

import com.sap.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId);

    Optional<Notificacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    long countByUsuarioIdAndLidaFalse(Long usuarioId);

    List<Notificacao> findByUsuarioIdAndCategoriaAndReferenciaTipo(
            Long usuarioId,
            String categoria,
            String referenciaTipo
    );

    boolean existsByUsuarioIdAndReferenciaTipoAndReferenciaId(
            Long usuarioId,
            String referenciaTipo,
            Long referenciaId
    );
}
