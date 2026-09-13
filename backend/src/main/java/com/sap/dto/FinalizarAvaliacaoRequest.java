package com.sap.dto;

import java.util.List;

public class FinalizarAvaliacaoRequest {

    private Long usuarioId;
    private Long avaliacaoId;
    private List<RespostaQuestaoRequest> respostas;

    public FinalizarAvaliacaoRequest() {
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getAvaliacaoId() {
        return avaliacaoId;
    }

    public void setAvaliacaoId(Long avaliacaoId) {
        this.avaliacaoId = avaliacaoId;
    }

    public List<RespostaQuestaoRequest> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<RespostaQuestaoRequest> respostas) {
        this.respostas = respostas;
    }
}