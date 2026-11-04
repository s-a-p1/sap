
package com.sap.repository;

import com.sap.model.DesempenhoAssunto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesempenhoAssuntoRepository extends JpaRepository<DesempenhoAssunto, Long> {

    List<DesempenhoAssunto> findByResultado_Id(Long resultadoId);

    List<DesempenhoAssunto> findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(Long usuarioId);
}
