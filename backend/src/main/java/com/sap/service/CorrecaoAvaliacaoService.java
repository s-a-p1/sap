package com.sap.service;

import com.sap.dto.FinalizarAvaliacaoRequest;
import com.sap.dto.RespostaQuestaoRequest;
import com.sap.dto.ResultadoAvaliacaoResponse;
import com.sap.model.AnaliseDesempenho;
import com.sap.model.Avaliacao;
import com.sap.model.Questao;
import com.sap.model.Resultado;
import com.sap.model.Usuario;
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
    private final AuditoriaService auditoriaService;

    public CorrecaoAvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            QuestaoRepository questaoRepository,
            ResultadoRepository resultadoRepository,
            AnaliseDesempenhoService analiseDesempenhoService,
            AuditoriaService auditoriaService) {

        this.avaliacaoRepository = avaliacaoRepository;
        this.questaoRepository = questaoRepository;
        this.resultadoRepository = resultadoRepository;
        this.analiseDesempenhoService = analiseDesempenhoService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ResultadoAvaliacaoResponse corrigir(
            FinalizarAvaliacaoRequest request,
            Usuario usuario) {

        Avaliacao avaliacao = avaliacaoRepository
                .findById(request.getAvaliacaoId())
                .orElseThrow(() ->
                        new RuntimeException("Avaliação não encontrada"));

        List<Questao> questoes =
                questaoRepository.findByAvaliacao_Id(
                        request.getAvaliacaoId());

        int totalQuestoes = questoes.size();

        if (totalQuestoes == 0) {
            throw new RuntimeException(
                    "A avaliação não possui questões cadastradas");
        }

        int acertos = 0;

        for (RespostaQuestaoRequest resposta : request.getRespostas()) {

            Questao questao = questoes.stream()
                    .filter(q ->
                            q.getId().equals(
                                    resposta.getQuestaoId()))
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
                usuario.getId(),
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

        auditoriaService.registrar(
                usuario.getId(),
                usuario.getEmail(),
                "FINALIZOU_AVALIACAO",
                "Avaliação " + avaliacao.getId()
                        + " finalizada. Resultado: "
                        + percentual + "%."
        );

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