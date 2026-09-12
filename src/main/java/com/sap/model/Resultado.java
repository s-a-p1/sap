package com.sap.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultados")
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao;

    @Column(name = "total_questoes", nullable = false)
    private Integer totalQuestoes;

    @Column(nullable = false)
    private Integer acertos;

    @Column(nullable = false)
    private Integer erros;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentual;

    @Column(name = "data_realizacao", nullable = false)
    private LocalDateTime dataRealizacao;

    public Resultado() {
    }

    public Resultado(
            Long usuarioId,
            Avaliacao avaliacao,
            Integer totalQuestoes,
            Integer acertos,
            Integer erros,
            BigDecimal percentual,
            LocalDateTime dataRealizacao) {

        this.usuarioId = usuarioId;
        this.avaliacao = avaliacao;
        this.totalQuestoes = totalQuestoes;
        this.acertos = acertos;
        this.erros = erros;
        this.percentual = percentual;
        this.dataRealizacao = dataRealizacao;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Integer getTotalQuestoes() {
        return totalQuestoes;
    }

    public void setTotalQuestoes(Integer totalQuestoes) {
        this.totalQuestoes = totalQuestoes;
    }

    public Integer getAcertos() {
        return acertos;
    }

    public void setAcertos(Integer acertos) {
        this.acertos = acertos;
    }

    public Integer getErros() {
        return erros;
    }

    public void setErros(Integer erros) {
        this.erros = erros;
    }

    public BigDecimal getPercentual() {
        return percentual;
    }

    public void setPercentual(BigDecimal percentual) {
        this.percentual = percentual;
    }

    public LocalDateTime getDataRealizacao() {
        return dataRealizacao;
    }

    public void setDataRealizacao(LocalDateTime dataRealizacao) {
        this.dataRealizacao = dataRealizacao;
    }
}
