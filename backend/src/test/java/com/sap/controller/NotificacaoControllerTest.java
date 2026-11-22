
package com.sap.controller;

import com.sap.model.Notificacao;
import com.sap.model.Usuario;
import com.sap.service.GeradorNotificacoesService;
import com.sap.service.NotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificacaoControllerTest {

    private NotificacaoService notificacaoService;
    private GeradorNotificacoesService geradorNotificacoesService;
    private NotificacaoController controller;
    private Authentication authentication;

    @BeforeEach
    void configurar() {
        notificacaoService = mock(NotificacaoService.class);
        geradorNotificacoesService =
                mock(GeradorNotificacoesService.class);
        authentication = mock(Authentication.class);

        controller = new NotificacaoController(
                notificacaoService,
                geradorNotificacoesService
        );
    }

    @Test
    void deveNegarListagemSemAutenticacao() {
        when(notificacaoService.buscarUsuarioAutenticado(authentication))
                .thenReturn(Optional.empty());

        ResponseEntity<?> resposta = controller.listar(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(geradorNotificacoesService);
        verify(notificacaoService, never()).listar(anyLong());
    }

    @Test
    void deveListarNotificacoesVazias() {
        autenticarUsuario();

        when(notificacaoService.listar(1L))
                .thenReturn(List.of());

        ResponseEntity<?> resposta = controller.listar(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(List.of(), resposta.getBody());

        verify(geradorNotificacoesService).gerarLembretesMetas(1L);
        verify(geradorNotificacoesService).gerarRecomendacoesRevisao(1L);
        verify(notificacaoService).listar(1L);
    }

    @Test
    void deveListarNotificacoesExistentes() {
        autenticarUsuario();

        Notificacao primeira = notificacao(10L, false);
        Notificacao segunda = notificacao(11L, true);

        when(notificacaoService.listar(1L))
                .thenReturn(List.of(primeira, segunda));

        ResponseEntity<?> resposta = controller.listar(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        List<?> lista = (List<?>) resposta.getBody();

        assertNotNull(lista);
        assertEquals(2, lista.size());

        Map<?, ?> dadosPrimeira = (Map<?, ?>) lista.get(0);
        Map<?, ?> dadosSegunda = (Map<?, ?>) lista.get(1);

        assertEquals(10L, dadosPrimeira.get("id"));
        assertEquals("Revisar conteúdo", dadosPrimeira.get("titulo"));
        assertEquals("Você possui uma meta pendente.",
                dadosPrimeira.get("mensagem"));
        assertEquals(false, dadosPrimeira.get("lida"));

        assertEquals(11L, dadosSegunda.get("id"));
        assertEquals(true, dadosSegunda.get("lida"));
    }

    @Test
    void deveGerarNotificacoesAntesDeListar() {
        autenticarUsuario();

        controller.listar(authentication);

        var ordem = inOrder(
                geradorNotificacoesService,
                notificacaoService
        );

        ordem.verify(geradorNotificacoesService)
                .gerarLembretesMetas(1L);
        ordem.verify(geradorNotificacoesService)
                .gerarRecomendacoesRevisao(1L);
        ordem.verify(notificacaoService).listar(1L);
    }

    @Test
    void deveNegarContagemSemAutenticacao() {
        when(notificacaoService.buscarUsuarioAutenticado(authentication))
                .thenReturn(Optional.empty());

        ResponseEntity<?> resposta =
                controller.contarNaoLidas(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(geradorNotificacoesService);
    }

    @Test
    void deveContarNotificacoesNaoLidas() {
        autenticarUsuario();

        when(notificacaoService.contarNaoLidas(1L))
                .thenReturn(5L);

        ResponseEntity<?> resposta =
                controller.contarNaoLidas(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(Map.of("quantidade", 5L), resposta.getBody());

        verify(notificacaoService).contarNaoLidas(1L);
    }

    @Test
    void deveRetornarZeroQuandoNaoExistemNotificacoesNaoLidas() {
        autenticarUsuario();

        when(notificacaoService.contarNaoLidas(1L))
                .thenReturn(0L);

        ResponseEntity<?> resposta =
                controller.contarNaoLidas(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(Map.of("quantidade", 0L), resposta.getBody());
    }

    @Test
    void deveGerarNotificacoesAntesDeContarNaoLidas() {
        autenticarUsuario();

        controller.contarNaoLidas(authentication);

        var ordem = inOrder(
                geradorNotificacoesService,
                notificacaoService
        );

        ordem.verify(geradorNotificacoesService)
                .gerarLembretesMetas(1L);
        ordem.verify(geradorNotificacoesService)
                .gerarRecomendacoesRevisao(1L);
        ordem.verify(notificacaoService).contarNaoLidas(1L);
    }

    @Test
    void deveNegarMarcacaoComoLidaSemAutenticacao() {
        when(notificacaoService.buscarUsuarioAutenticado(authentication))
                .thenReturn(Optional.empty());

        ResponseEntity<?> resposta =
                controller.marcarComoLida(10L, authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verify(notificacaoService, never())
                .marcarComoLida(anyLong(), anyLong());
    }

    @Test
    void deveMarcarNotificacaoComoLida() {
        autenticarUsuario();

        when(notificacaoService.marcarComoLida(10L, 1L))
                .thenReturn(true);

        ResponseEntity<?> resposta =
                controller.marcarComoLida(10L, authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(
                Map.of("mensagem", "Notificação marcada como lida."),
                resposta.getBody()
        );

        verify(notificacaoService).marcarComoLida(10L, 1L);
    }

    @Test
    void deveRetornarNaoEncontradoParaNotificacaoInexistente() {
        autenticarUsuario();

        when(notificacaoService.marcarComoLida(99L, 1L))
                .thenReturn(false);

        ResponseEntity<?> resposta =
                controller.marcarComoLida(99L, authentication);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Notificação não encontrada.");
    }

    @Test
    void deveUtilizarIdDoUsuarioAutenticado() {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getId()).thenReturn(25L);
        when(notificacaoService.buscarUsuarioAutenticado(authentication))
                .thenReturn(Optional.of(usuario));

        when(notificacaoService.marcarComoLida(10L, 25L))
                .thenReturn(true);

        ResponseEntity<?> resposta =
                controller.marcarComoLida(10L, authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        verify(notificacaoService).marcarComoLida(10L, 25L);
    }

    @Test
    void naoDeveGerarNovasNotificacoesAoMarcarComoLida() {
        autenticarUsuario();

        controller.marcarComoLida(10L, authentication);

        verifyNoInteractions(geradorNotificacoesService);
    }

    private void autenticarUsuario() {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getId()).thenReturn(1L);

        when(notificacaoService.buscarUsuarioAutenticado(authentication))
                .thenReturn(Optional.of(usuario));
    }

    private Notificacao notificacao(Long id, boolean lida) {
        Notificacao notificacao = mock(Notificacao.class);

        when(notificacao.getId()).thenReturn(id);
        when(notificacao.getTitulo()).thenReturn("Revisar conteúdo");
        when(notificacao.getMensagem())
                .thenReturn("Você possui uma meta pendente.");
        when(notificacao.isLida()).thenReturn(lida);

        return notificacao;
    }

    private void assertErro(
            ResponseEntity<?> resposta,
            HttpStatus status,
            String mensagem) {

        assertEquals(status, resposta.getStatusCode());
        assertEquals(Map.of("erro", mensagem), resposta.getBody());
    }
}
