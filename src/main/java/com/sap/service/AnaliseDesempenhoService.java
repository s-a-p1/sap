package com.sap.service;

import org.springframework.stereotype.Service;

import com.sap.model.AnaliseDesempenho;
import com.sap.repository.AnaliseDesempenhoRepository;

@Service
public class AnaliseDesempenhoService {

    private final AnaliseDesempenhoRepository repository;

    public AnaliseDesempenhoService(AnaliseDesempenhoRepository repository) {
        this.repository = repository;
    }

    public AnaliseDesempenho analisar(Integer acertos, Integer totalQuestoes) {

        if (acertos == null || totalQuestoes == null) {
    throw new IllegalArgumentException("Acertos e total de questoes sao obrigatorios.");
}

if (totalQuestoes <= 0) {
    throw new IllegalArgumentException("O total de questoes deve ser maior que zero.");
}

if (acertos < 0) {
    throw new IllegalArgumentException("A quantidade de acertos nao pode ser negativa.");
}

if (acertos > totalQuestoes) {
    throw new IllegalArgumentException("A quantidade de acertos nao pode ser maior que o total de questoes.");
}

        double percentual = (acertos * 100.0) / totalQuestoes;

        AnaliseDesempenho analise = new AnaliseDesempenho();

        analise.setAcertos(acertos);
        analise.setTotalQuestoes(totalQuestoes);
        analise.setPercentual(percentual);
        if (percentual < 50) {
    analise.setDificuldade("ALTA");
    analise.setPrioridade("ALTA");
    analise.setRecomendacao(
        "Revise os conceitos fundamentais antes de realizar uma nova avaliacao."
    );

} else if (percentual < 70) {
    analise.setDificuldade("MEDIA");
    analise.setPrioridade("MEDIA");
    analise.setRecomendacao(
        "Revise os pontos de erro e continue praticando."
    );

} else {
    analise.setDificuldade("BAIXA");
    analise.setPrioridade("BAIXA");
    analise.setRecomendacao(
        "Bom dominio do conteudo. Voce pode avancar para conteudos mais complexos."
    );
}

        return repository.save(analise);
    }
}