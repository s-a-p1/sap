package com.sap.dto;

import java.math.BigDecimal;

public class ResultadoAvaliacaoResponse {

    private Long resultadoId;
    private Integer totalQuestoes;
    private Integer acertos;
    private Integer erros;
    private BigDecimal percentual;

    public ResultadoAvaliacaoResponse(
            Long resultadoId,
            Integer totalQuestoes,
            Integer acertos,
            Integer erros,
            BigDecimal percentual) {

        this.resultadoId = resultadoId;
        this.totalQuestoes = totalQuestoes;
        this.acertos = acertos;
        this.erros = erros;
        this.percentual = percentual;
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
}