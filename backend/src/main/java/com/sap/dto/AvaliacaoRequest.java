
package com.sap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AvaliacaoRequest {

    @NotBlank(message = "O título da avaliação é obrigatório")
    @Size(max = 150, message = "O título da avaliação deve ter no máximo 150 caracteres")
    private String titulo;

    @NotNull(message = "O assunto da avaliação é obrigatório")
    @Positive(message = "Informe um assunto válido")
    private Long assuntoId;

    public AvaliacaoRequest() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Long getAssuntoId() {
        return assuntoId;
    }

    public void setAssuntoId(Long assuntoId) {
        this.assuntoId = assuntoId;
    }
}
