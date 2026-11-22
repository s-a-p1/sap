
package com.sap.controller;

import com.sap.dto.RedefinicaoSenhaDTO;
import com.sap.dto.SolicitacaoRecuperacaoSenhaDTO;
import com.sap.service.RecuperacaoSenhaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecuperacaoSenhaControllerTest {

    private RecuperacaoSenhaService recuperacaoSenhaService;
    private RecuperacaoSenhaController controller;

    private static final String MENSAGEM_RECUPERACAO =
            "Se o e-mail estiver cadastrado, você receberá as instruções para recuperar sua senha.";

    private static final String MENSAGEM_ERRO =
            "Link inválido ou expirado, ou senha fora dos requisitos.";

    private static final String MENSAGEM_SUCESSO =
            "Senha redefinida com sucesso.";

    @BeforeEach
    void configurar() {
        recuperacaoSenhaService = mock(RecuperacaoSenhaService.class);

        controller = new RecuperacaoSenhaController(
                recuperacaoSenhaService
        );
    }

    @Test
    void deveSolicitarRecuperacaoComSucesso() {
        SolicitacaoRecuperacaoSenhaDTO dados =
                solicitacao("estudante@email.com");

        ResponseEntity<Map<String, String>> resposta =
                controller.solicitarRecuperacao(dados);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(
                Map.of("mensagem", MENSAGEM_RECUPERACAO),
                resposta.getBody()
        );

        verify(recuperacaoSenhaService)
                .solicitarRecuperacao("estudante@email.com");
    }

    @Test
    void deveEnviarEmailCorretoAoService() {
        SolicitacaoRecuperacaoSenhaDTO dados =
                solicitacao("usuario@gmail.com");

        controller.solicitarRecuperacao(dados);

        verify(recuperacaoSenhaService)
                .solicitarRecuperacao("usuario@gmail.com");
    }

    @Test
    void deveRetornarMensagemGenericaNaRecuperacao() {
        SolicitacaoRecuperacaoSenhaDTO dados =
                solicitacao("desconhecido@email.com");

        ResponseEntity<Map<String, String>> resposta =
                controller.solicitarRecuperacao(dados);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(
                MENSAGEM_RECUPERACAO,
                resposta.getBody().get("mensagem")
        );
    }

    @Test
    void deveChamarServiceUmaUnicaVezNaSolicitacao() {
        SolicitacaoRecuperacaoSenhaDTO dados =
                solicitacao("estudante@email.com");

        controller.solicitarRecuperacao(dados);

        verify(recuperacaoSenhaService, times(1))
                .solicitarRecuperacao("estudante@email.com");

        verifyNoMoreInteractions(recuperacaoSenhaService);
    }

    @Test
    void devePropagarErroNaSolicitacao() {
        SolicitacaoRecuperacaoSenhaDTO dados =
                solicitacao("estudante@email.com");

        doThrow(new IllegalStateException("Serviço indisponível."))
                .when(recuperacaoSenhaService)
                .solicitarRecuperacao("estudante@email.com");

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> controller.solicitarRecuperacao(dados)
        );

        assertEquals("Serviço indisponível.", erro.getMessage());
    }

    @Test
    void deveRedefinirSenhaComSucesso() {
        RedefinicaoSenhaDTO dados =
                redefinicao("token-valido", "NovaSenha@123");

        when(recuperacaoSenhaService.redefinirSenha(
                "token-valido", "NovaSenha@123"))
                .thenReturn(true);

        ResponseEntity<Map<String, String>> resposta =
                controller.redefinirSenha(dados);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(
                Map.of("mensagem", MENSAGEM_SUCESSO),
                resposta.getBody()
        );
    }

    @Test
    void deveEnviarTokenESenhaCorretosAoService() {
        RedefinicaoSenhaDTO dados =
                redefinicao("abc123", "SenhaSegura@456");

        when(recuperacaoSenhaService.redefinirSenha(
                "abc123", "SenhaSegura@456"))
                .thenReturn(true);

        controller.redefinirSenha(dados);

        verify(recuperacaoSenhaService)
                .redefinirSenha("abc123", "SenhaSegura@456");
    }

    @Test
    void deveRejeitarRedefinicaoQuandoServiceRetornaFalse() {
        RedefinicaoSenhaDTO dados =
                redefinicao("token-invalido", "NovaSenha@123");

        when(recuperacaoSenhaService.redefinirSenha(
                "token-invalido", "NovaSenha@123"))
                .thenReturn(false);

        ResponseEntity<Map<String, String>> resposta =
                controller.redefinirSenha(dados);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(
                Map.of("mensagem", MENSAGEM_ERRO),
                resposta.getBody()
        );
    }

    @Test
    void deveRetornarMensagemDeErroParaSenhaRejeitada() {
        RedefinicaoSenhaDTO dados =
                redefinicao("token-valido", "123");

        when(recuperacaoSenhaService.redefinirSenha(
                "token-valido", "123"))
                .thenReturn(false);

        ResponseEntity<Map<String, String>> resposta =
                controller.redefinirSenha(dados);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(
                MENSAGEM_ERRO,
                resposta.getBody().get("mensagem")
        );
    }

    @Test
    void deveRetornarMensagemDeErroParaTokenExpirado() {
        RedefinicaoSenhaDTO dados =
                redefinicao("token-expirado", "NovaSenha@123");

        when(recuperacaoSenhaService.redefinirSenha(
                "token-expirado", "NovaSenha@123"))
                .thenReturn(false);

        ResponseEntity<Map<String, String>> resposta =
                controller.redefinirSenha(dados);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(
                MENSAGEM_ERRO,
                resposta.getBody().get("mensagem")
        );
    }

    @Test
    void deveChamarServiceUmaUnicaVezNaRedefinicao() {
        RedefinicaoSenhaDTO dados =
                redefinicao("token-valido", "NovaSenha@123");

        when(recuperacaoSenhaService.redefinirSenha(
                "token-valido", "NovaSenha@123"))
                .thenReturn(true);

        controller.redefinirSenha(dados);

        verify(recuperacaoSenhaService, times(1))
                .redefinirSenha("token-valido", "NovaSenha@123");

        verifyNoMoreInteractions(recuperacaoSenhaService);
    }

    @Test
    void devePropagarErroNaRedefinicao() {
        RedefinicaoSenhaDTO dados =
                redefinicao("token-valido", "NovaSenha@123");

        when(recuperacaoSenhaService.redefinirSenha(
                "token-valido", "NovaSenha@123"))
                .thenThrow(new IllegalStateException(
                        "Erro ao redefinir senha."
                ));

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> controller.redefinirSenha(dados)
        );

        assertEquals(
                "Erro ao redefinir senha.",
                erro.getMessage()
        );
    }

    private SolicitacaoRecuperacaoSenhaDTO solicitacao(String email) {
        SolicitacaoRecuperacaoSenhaDTO dados =
                mock(SolicitacaoRecuperacaoSenhaDTO.class);

        when(dados.email()).thenReturn(email);

        return dados;
    }

    private RedefinicaoSenhaDTO redefinicao(
            String token,
            String novaSenha) {

        RedefinicaoSenhaDTO dados =
                mock(RedefinicaoSenhaDTO.class);

        when(dados.token()).thenReturn(token);
        when(dados.novaSenha()).thenReturn(novaSenha);

        return dados;
    }
}
