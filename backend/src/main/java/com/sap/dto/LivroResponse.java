package com.sap.dto;

public class LivroResponse {

    private String titulo;
    private String autor;
    private Integer anoPublicacao;
    private String chave;

    public LivroResponse(
            String titulo,
            String autor,
            Integer anoPublicacao,
            String chave) {

        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.chave = chave;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }

    public String getChave() {
        return chave;
    }
}