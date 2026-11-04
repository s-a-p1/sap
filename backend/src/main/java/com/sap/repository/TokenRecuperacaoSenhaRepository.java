
package com.sap.repository;

import com.sap.model.TokenRecuperacaoSenha;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface TokenRecuperacaoSenhaRepository
        extends JpaRepository<TokenRecuperacaoSenha, Long> {

    Optional<TokenRecuperacaoSenha> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TokenRecuperacaoSenha t WHERE t.tokenHash = :tokenHash")
    Optional<TokenRecuperacaoSenha> findByTokenHashComBloqueio(
            @Param("tokenHash") String tokenHash
    );

    List<TokenRecuperacaoSenha> findByUsuarioIdAndDataUtilizacaoIsNull(
            Long usuarioId
    );

        boolean existsByUsuarioIdAndDataCriacaoAfter(
                Long usuarioId,
                LocalDateTime dataLimite
        );

        long countByUsuarioIdAndDataCriacaoAfter(
                Long usuarioId,
                LocalDateTime dataLimite
);

}
