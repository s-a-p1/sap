package com.sap.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 255)
    private String senha;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProvedorAutenticacao provedor = ProvedorAutenticacao.LOCAL;

    @Column(name = "provedor_id", length = 255)
    private String provedorId;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "aceitou_termos", nullable = false)
    private boolean aceitouTermos = false;

    @Column(name = "data_aceite_termos")
    private LocalDateTime dataAceiteTermos;

    @Column(name = "versao_termos", length = 20)
    private String versaoTermos;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senha, String tipo) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public ProvedorAutenticacao getProvedor() {
        return provedor;
    }

    public void setProvedor(ProvedorAutenticacao provedor) {
        this.provedor = provedor;
    }

    public String getProvedorId() {
        return provedorId;
    }

    public void setProvedorId(String provedorId) {
        this.provedorId = provedorId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isAceitouTermos() {
        return aceitouTermos;
    }

    public void setAceitouTermos(boolean aceitouTermos) {
        this.aceitouTermos = aceitouTermos;
    }

    public LocalDateTime getDataAceiteTermos() {
        return dataAceiteTermos;
    }

    public void setDataAceiteTermos(LocalDateTime dataAceiteTermos) {
        this.dataAceiteTermos = dataAceiteTermos;
    }

    public String getVersaoTermos() {
        return versaoTermos;
    }

    public void setVersaoTermos(String versaoTermos) {
        this.versaoTermos = versaoTermos;
    }
}