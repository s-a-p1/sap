package com.sap.dto;

public record AlterarSenhaRequest(
        String senhaAtual,
        String novaSenha,
        String confirmarSenha
) {
}
