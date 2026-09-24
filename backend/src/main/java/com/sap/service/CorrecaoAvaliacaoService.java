package com.sap.service;

import com.sap.dto.FinalizarAvaliacaoRequest;
import com.sap.dto.RespostaQuestaoRequest;
import com.sap.dto.ResultadoAvaliacaoResponse;
import com.sap.model.AnaliseDesempenho;
import com.sap.model.Avaliacao;
import com.sap.model.Questao;
import com.sap.model.Resultado;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.QuestaoRepository;
import com.sap.repository.ResultadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CorrecaoAvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final QuestaoRepository questaoRepository;
    private final ResultadoRepository resultadoRepository;
    private final AnaliseDesempenhoService analiseDesempenhoService;

    public CorrecaoAvaliacaoService(
        AvaliacaoRepository avaliacaoRepository,
        QuestaoRepository questaoRepository,
        ResultadoRepository resultadoRepository,
        AnaliseDesempenhoService analiseDesempenhoService) {

        this.avaliacaoRepository = avaliacaoRepository;
        this.questaoRepository = questaoRepository;
        this.resultadoRepository = resultadoRepository;
        this.analiseDesempenhoService = analiseDesempenhoService;
  
    }

    @Transactional
    public ResultadoAvaliacaoResponse corrigir(
            FinalizarAvaliacaoRequest request) {

        Avaliacao avaliacao = avaliacaoRepository
                .findById(request.getAvaliacaoId())
                .orElseThrow(() ->
                        new RuntimeException("Avaliação não encontrada"));

        List<Questao> questoes =
                questaoRepository.findByAvaliacao_Id(
                        request.getAvaliacaoId());

        int totalQuestoes = questoes.size();
        int acertos = 0;

        for (RespostaQuestaoRequest resposta : request.getRespostas()) {

            Questao questao = questoes.stream()
                    .filter(q -> q.getId().equals(resposta.getQuestaoId()))
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Questão não pertence à avaliação"));

            if (questao.getRespostaCorreta()
                    .equalsIgnoreCase(resposta.getResposta())) {

                acertos++;
            }
        }

        int erros = totalQuestoes - acertos;

        BigDecimal percentual = BigDecimal
                .valueOf(acertos)
                .divide(
                        BigDecimal.valueOf(totalQuestoes),
                        4,
                        RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        Resultado resultado = new Resultado(
                request.getUsuarioId(),
                avaliacao,
                totalQuestoes,
                acertos,
                erros,
                percentual,
                LocalDateTime.now()
        );

        resultado = resultadoRepository.save(resultado);

        AnaliseDesempenho analise =
                analiseDesempenhoService.analisarResultado(resultado);

        return new ResultadoAvaliacaoResponse(
        resultado.getId(),
        totalQuestoes,
        acertos,
        erros,
        percentual,
        analise.getDificuldade(),
        analise.getPrioridade(),
        analise.getRecomendacao()
        );
    }
}
