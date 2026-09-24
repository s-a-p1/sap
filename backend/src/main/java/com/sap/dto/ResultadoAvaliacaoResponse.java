package com.sap.dto;

import java.math.BigDecimal;

public class ResultadoAvaliacaoResponse {

    private Long resultadoId;
    private Integer totalQuestoes;
    private Integer acertos;
    private Integer erros;
    private BigDecimal percentual;

    private String dificuldade;
    private String prioridade;
    private String recomendacao;

    public ResultadoAvaliacaoResponse(
            Long resultadoId,
            Integer totalQuestoes,
            Integer acertos,
            Integer erros,
            BigDecimal percentual,
            String dificuldade,
            String prioridade,
            String recomendacao) {

        this.resultadoId = resultadoId;
        this.totalQuestoes = totalQuestoes;
        this.acertos = acertos;
        this.erros = erros;
        this.percentual = percentual;
        this.dificuldade = dificuldade;
        this.prioridade = prioridade;
        this.recomendacao = recomendacao;
    }

    public Long getResultadoId() {
        return resultadoId;
    }

    public Integer getTotalQuestoes() {
        return totalQuestoes;
    }

    public Integer getAcertos() {
        return acertos;
    }

    public Integer getErros() {
        return erros;
    }

    public BigDecimal getPercentual() {
        return percentual;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public String getRecomendacao() {
        return recomendacao;
    }
}