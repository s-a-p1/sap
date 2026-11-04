
package com.sap.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "semestres_cursos",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_semestre_curso",
        columnNames = {"curso_id", "numero"}
    )
)
public class SemestreCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(nullable = false)
    private Integer numero;

    public SemestreCurso() {
    }

    public SemestreCurso(Curso curso, Integer numero) {
        this.curso = curso;
        this.numero = numero;
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

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }
}
