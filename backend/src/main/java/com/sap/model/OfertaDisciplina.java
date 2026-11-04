package com.sap.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "ofertas_disciplinas",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_oferta_curso_semestre_disciplina",
        columnNames = {"curso_id", "semestre", "disciplina_id"}
    )
)
public class OfertaDisciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(nullable = false)
    private Integer semestre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    public OfertaDisciplina() {
    }

    public OfertaDisciplina(Curso curso, Integer semestre, Disciplina disciplina) {
        this.curso = curso;
        this.semestre = semestre;
        this.disciplina = disciplina;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }
}