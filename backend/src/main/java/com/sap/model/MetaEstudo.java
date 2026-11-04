
package com.sap.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "metas_estudo")
public class MetaEstudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assunto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_origem_id")
    private Resultado resultadoOrigem;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 20)
    private String prioridade;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(nullable = false)
    private Boolean concluida = false;

    @Column(nullable = false, length = 20)
    private String origem = "AUTOMATICA";

    public MetaEstudo() {
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

    public Assunto getAssunto() {
        return assunto;
    }

    public void setAssunto(Assunto assunto) {
        this.assunto = assunto;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDate dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public Boolean getConcluida() {
        return concluida;
    }

    public void setConcluida(Boolean concluida) {
        this.concluida = concluida;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public Resultado getResultadoOrigem() {
    return resultadoOrigem;
    }

    public void setResultadoOrigem(Resultado resultadoOrigem) {
    this.resultadoOrigem = resultadoOrigem;
    }
}
