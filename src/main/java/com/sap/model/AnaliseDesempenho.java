package com.sap.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AnaliseDesempenho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

private Integer acertos;

private Integer totalQuestoes;

private Double percentual;

private String dificuldade;

private String prioridade;

private String recomendacao;
public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public Integer getAcertos() {
    return acertos;
}

public void setAcertos(Integer acertos) {
    this.acertos = acertos;
}

public Integer getTotalQuestoes() {
    return totalQuestoes;
}

public void setTotalQuestoes(Integer totalQuestoes) {
    this.totalQuestoes = totalQuestoes;
}

public Double getPercentual() {
    return percentual;
}

public void setPercentual(Double percentual) {
    this.percentual = percentual;
}

public String getDificuldade() {
    return dificuldade;
}

public void setDificuldade(String dificuldade) {
    this.dificuldade = dificuldade;
}

public String getPrioridade() {
    return prioridade;
}

public void setPrioridade(String prioridade) {
    this.prioridade = prioridade;
}

public String getRecomendacao() {
    return recomendacao;
}

public void setRecomendacao(String recomendacao) {
    this.recomendacao = recomendacao;
}
}