
package com.sap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "preferencias_notificacoes")
public class PreferenciaNotificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private boolean notificacoesAtivas = true;

    @Column(nullable = false)
    private boolean lembretesPlano = true;

    @Column(nullable = false)
    private boolean metasPendentes = true;

    @Column(nullable = false)
    private boolean recomendacoesRevisao = true;

    @Column(nullable = false)
    private boolean avaliacoesProximas = false;

    @Column(nullable = false)
    private boolean evolucaoDesempenho = true;

    @Column(nullable = false)
    private boolean resumoSemanal = false;

    @Column(nullable = false)
    private int antecedenciaLembretes = 1;

    public PreferenciaNotificacao() {
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean isNotificacoesAtivas() {
        return notificacoesAtivas;
    }

    public void setNotificacoesAtivas(boolean notificacoesAtivas) {
        this.notificacoesAtivas = notificacoesAtivas;
    }

    public boolean isLembretesPlano() {
        return lembretesPlano;
    }

    public void setLembretesPlano(boolean lembretesPlano) {
        this.lembretesPlano = lembretesPlano;
    }

    public boolean isMetasPendentes() {
        return metasPendentes;
    }

    public void setMetasPendentes(boolean metasPendentes) {
        this.metasPendentes = metasPendentes;
    }

    public boolean isRecomendacoesRevisao() {
        return recomendacoesRevisao;
    }

    public void setRecomendacoesRevisao(boolean recomendacoesRevisao) {
        this.recomendacoesRevisao = recomendacoesRevisao;
    }

    public boolean isAvaliacoesProximas() {
        return avaliacoesProximas;
    }

    public void setAvaliacoesProximas(boolean avaliacoesProximas) {
        this.avaliacoesProximas = avaliacoesProximas;
    }

    public boolean isEvolucaoDesempenho() {
        return evolucaoDesempenho;
    }

    public void setEvolucaoDesempenho(boolean evolucaoDesempenho) {
        this.evolucaoDesempenho = evolucaoDesempenho;
    }

    public boolean isResumoSemanal() {
        return resumoSemanal;
    }

    public void setResumoSemanal(boolean resumoSemanal) {
        this.resumoSemanal = resumoSemanal;
    }

    public int getAntecedenciaLembretes() {
        return antecedenciaLembretes;
    }

    public void setAntecedenciaLembretes(int antecedenciaLembretes) {
        this.antecedenciaLembretes = antecedenciaLembretes;
    }
}
