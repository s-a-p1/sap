package com.sap.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "matriculas_disciplinas",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_matricula_configuracao_oferta",
        columnNames = {"configuracao_id", "oferta_disciplina_id"}
    )
)
public class MatriculaDisciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuracao_id", nullable = false)
    private ConfiguracaoAcademica configuracaoAcademica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_disciplina_id", nullable = false)
    private OfertaDisciplina ofertaDisciplina;

    public MatriculaDisciplina() {
    }

    public MatriculaDisciplina(
            ConfiguracaoAcademica configuracaoAcademica,
            OfertaDisciplina ofertaDisciplina) {
        this.configuracaoAcademica = configuracaoAcademica;
        this.ofertaDisciplina = ofertaDisciplina;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConfiguracaoAcademica getConfiguracaoAcademica() {
        return configuracaoAcademica;
    }

    public void setConfiguracaoAcademica(
            ConfiguracaoAcademica configuracaoAcademica) {
        this.configuracaoAcademica = configuracaoAcademica;
    }

    public OfertaDisciplina getOfertaDisciplina() {
        return ofertaDisciplina;
    }

    public void setOfertaDisciplina(OfertaDisciplina ofertaDisciplina) {
        this.ofertaDisciplina = ofertaDisciplina;
    }
}