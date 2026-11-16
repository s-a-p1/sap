
package com.sap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class QuestaoEdicaoRequest {

    @NotBlank(message = "O enunciado da questão é obrigatório")
    @Size(max = 5000, message = "O enunciado deve ter no máximo 5000 caracteres")
    private String enunciado;

    @NotBlank(message = "A alternativa A é obrigatória")
    @Size(max = 255, message = "A alternativa A deve ter no máximo 255 caracteres")
    private String alternativaA;

    @NotBlank(message = "A alternativa B é obrigatória")
    @Size(max = 255, message = "A alternativa B deve ter no máximo 255 caracteres")
    private String alternativaB;

    @NotBlank(message = "A alternativa C é obrigatória")
    @Size(max = 255, message = "A alternativa C deve ter no máximo 255 caracteres")
    private String alternativaC;

    @NotBlank(message = "A alternativa D é obrigatória")
    @Size(max = 255, message = "A alternativa D deve ter no máximo 255 caracteres")
    private String alternativaD;

    @NotBlank(message = "A resposta correta é obrigatória")
    @Pattern(
            regexp = "(?i)[ABCD]",
            message = "A resposta correta deve ser A, B, C ou D"
    )
    private String respostaCorreta;

    public QuestaoEdicaoRequest() {
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getAlternativaA() {
        return alternativaA;
    }

    public void setAlternativaA(String alternativaA) {
        this.alternativaA = alternativaA;
    }

    public String getAlternativaB() {
        return alternativaB;
    }

    public void setAlternativaB(String alternativaB) {
        this.alternativaB = alternativaB;
    }

    public String getAlternativaC() {
        return alternativaC;
    }

    public void setAlternativaC(String alternativaC) {
        this.alternativaC = alternativaC;
    }

    public String getAlternativaD() {
        return alternativaD;
    }

    public void setAlternativaD(String alternativaD) {
        this.alternativaD = alternativaD;
    }

    public String getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(String respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }
}
