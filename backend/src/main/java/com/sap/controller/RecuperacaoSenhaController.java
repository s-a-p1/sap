
package com.sap.controller;

import com.sap.dto.RedefinicaoSenhaDTO;
import com.sap.dto.SolicitacaoRecuperacaoSenhaDTO;
import com.sap.service.RecuperacaoSenhaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RecuperacaoSenhaController {

    private final RecuperacaoSenhaService recuperacaoSenhaService;

    public RecuperacaoSenhaController(RecuperacaoSenhaService recuperacaoSenhaService) {
        this.recuperacaoSenhaService = recuperacaoSenhaService;
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<Map<String, String>> solicitarRecuperacao(
            @RequestBody SolicitacaoRecuperacaoSenhaDTO dados) {

        recuperacaoSenhaService.solicitarRecuperacao(dados.email());

        return ResponseEntity.ok(Map.of(
                "mensagem",
                "Se o e-mail estiver cadastrado, você receberá as instruções para recuperar sua senha."
        ));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(
            @RequestBody RedefinicaoSenhaDTO dados) {

        boolean redefinida = recuperacaoSenhaService.redefinirSenha(
                dados.token(),
                dados.novaSenha()
        );

        if (!redefinida) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem",
                    "Link inválido ou expirado, ou senha fora dos requisitos."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "mensagem",
                "Senha redefinida com sucesso."
        ));
    }
}
