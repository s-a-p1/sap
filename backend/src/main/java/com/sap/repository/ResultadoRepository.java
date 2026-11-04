
package com.sap.repository;

import com.sap.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {

    List<Resultado> findByUsuarioIdOrderByDataRealizacaoDesc(Long usuarioId);

    boolean existsByAvaliacao_Id(Long avaliacaoId);

}
