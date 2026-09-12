package com.sap.service;

import com.sap.dto.QuestaoResponse;
import com.sap.model.Questao;
import com.sap.repository.QuestaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestaoService {

    private final QuestaoRepository questaoRepository;

    public QuestaoService(QuestaoRepository questaoRepository) {
        this.questaoRepository = questaoRepository;
    }

    public List<QuestaoResponse> listarPorAvaliacao(Long avaliacaoId) {

        List<Questao> questoes =
                questaoRepository.findByAvaliacao_Id(avaliacaoId);

        return questoes.stream()
                .map(questao -> new QuestaoResponse(
                        questao.getId(),
                        questao.getEnunciado(),
                        questao.getAlternativaA(),
                        questao.getAlternativaB(),
                        questao.getAlternativaC(),
                        questao.getAlternativaD()
                ))
                .toList();
    }
}
