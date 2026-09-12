package com.sap.repository;

import com.sap.model.Questao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {

    List<Questao> findByAvaliacao_Id(Long avaliacaoId);
}
