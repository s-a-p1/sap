
package com.sap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "assuntos")
public class Assunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do assunto é obrigatório")
    @Size(max = 100, message = "O nome do assunto deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    @Column(length = 255)
    private String descricao;

    @Column(name = "visivel_catalogo", nullable = false)
    private Boolean visivelCatalogo = true;

    @NotNull(message = "A disciplina do assunto é obrigatória")
    @ManyToOne
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    public Assunto() {
    }

    public Assunto(String nome, String descricao, Disciplina disciplina) {
        this.nome = nome;
        this.descricao = descricao;
        this.disciplina = disciplina;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public Boolean getVisivelCatalogo() {
        return visivelCatalogo;
    }

    public void setVisivelCatalogo(Boolean visivelCatalogo) {
        this.visivelCatalogo = visivelCatalogo;
    }
}
