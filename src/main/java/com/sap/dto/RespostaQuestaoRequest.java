package com.sap.dto;

public class RespostaQuestaoRequest {

    private Long questaoId;
    private String resposta;

    public RespostaQuestaoRequest() {
    }

    public Long getQuestaoId() {
        return questaoId;
    }

    public void setQuestaoId(Long questaoId) {
        this.questaoId = questaoId;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }
}
