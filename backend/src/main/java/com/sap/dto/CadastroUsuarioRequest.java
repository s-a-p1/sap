package com.sap.dto;

import jakarta.validation.constraints.AssertTrue;
import com.sap.validation.ValidadorCredenciais;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CadastroUsuarioRequest {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Pattern(
            regexp = ValidadorCredenciais.EMAIL_REGEX,
            message = ValidadorCredenciais.MENSAGEM_EMAIL
    )
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(
            min = 6,
            message = "A senha deve possuir pelo menos 6 caracteres."
    )
    @Pattern(
            regexp = ValidadorCredenciais.SENHA_REGEX,
            message = ValidadorCredenciais.MENSAGEM_SENHA
    )
    private String senha;

    @AssertTrue(
            message = "É necessário aceitar os Termos de Uso e a Política de Privacidade."
    )
    private boolean aceitouTermos;

    public CadastroUsuarioRequest() {
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

    public boolean isAceitouTermos() {
        return aceitouTermos;
    }

    public void setAceitouTermos(boolean aceitouTermos) {
        this.aceitouTermos = aceitouTermos;
    }
}