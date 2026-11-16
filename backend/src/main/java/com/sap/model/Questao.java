package com.sap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "questoes")
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @NotBlank(message = "O enunciado da questão é obrigatório")
    @Size(max = 5000, message = "O enunciado deve ter no máximo 5000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @NotBlank(message = "A alternativa A é obrigatória")
    @Size(max = 255, message = "A alternativa A deve ter no máximo 255 caracteres")
    @Column(name = "alternativa_a", nullable = false, length = 255)
    private String alternativaA;

    @NotBlank(message = "A alternativa B é obrigatória")
    @Size(max = 255, message = "A alternativa B deve ter no máximo 255 caracteres")
    @Column(name = "alternativa_b", nullable = false, length = 255)
    private String alternativaB;

    @NotBlank(message = "A alternativa C é obrigatória")
    @Size(max = 255, message = "A alternativa C deve ter no máximo 255 caracteres")
    @Column(name = "alternativa_c", nullable = false, length = 255)
    private String alternativaC;

    @NotBlank(message = "A alternativa D é obrigatória")
    @Size(max = 255, message = "A alternativa D deve ter no máximo 255 caracteres")
    @Column(name = "alternativa_d", nullable = false, length = 255)
    private String alternativaD;

    @NotBlank(message = "A resposta correta é obrigatória")
    @Pattern(
            regexp = "[ABCD]",
            message = "A resposta correta deve ser A, B, C ou D"
    )
    @Column(name = "resposta_correta", nullable = false, length = 1)
    private String respostaCorreta;

    @NotNull(message = "A avaliação da questão é obrigatória")
    @ManyToOne
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao;

    @ManyToOne
    @JoinColumn(name = "assunto_id")
    private Assunto assunto;


    public Questao() {
    }

    public Questao(
            String enunciado,
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD,
            String respostaCorreta,
            Avaliacao avaliacao) {

        this.enunciado = enunciado;
        this.alternativaA = alternativaA;
        this.alternativaB = alternativaB;
        this.alternativaC = alternativaC;
        this.alternativaD = alternativaD;
        this.respostaCorreta = respostaCorreta;
        this.avaliacao = avaliacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }
    public Assunto getAssunto() {
    return assunto;
    }

    public void setAssunto(Assunto assunto) {
    this.assunto = assunto;
    }
}
