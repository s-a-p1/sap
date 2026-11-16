
package com.sap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título da avaliação é obrigatório")
    @Size(max = 150, message = "O título da avaliação deve ter no máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotNull(message = "O assunto da avaliação é obrigatório")
    @ManyToOne
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assunto;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    public Avaliacao() {
    }

    public Avaliacao(String titulo, Assunto assunto) {
        this.titulo = titulo;
        this.assunto = assunto;
    }

    @PrePersist
    public void registrarDataCriacao() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Assunto getAssunto() {
        return assunto;
    }

    public void setAssunto(Assunto assunto) {
        this.assunto = assunto;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
