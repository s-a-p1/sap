
package com.sap.controller;

import com.sap.dto.PreferenciaNotificacaoDTO;
import com.sap.model.PreferenciaNotificacao;
import com.sap.model.Usuario;
import com.sap.service.NotificacaoService;
import com.sap.service.PreferenciaNotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PreferenciaNotificacaoControllerTest {

    private PreferenciaNotificacaoService preferenciaService;
    private NotificacaoService notificacaoService;
    private PreferenciaNotificacaoController controller;
    private Authentication authentication;

    @BeforeEach
    void configurar() {
        preferenciaService = mock(PreferenciaNotificacaoService.class);
        notificacaoService = mock(NotificacaoService.class);
        authentication = mock(Authentication.class);

        controller = new PreferenciaNotificacaoController(
                preferenciaService,
                notificacaoService
        );
    }

    @Test
    void deveNegarConsultaSemAutenticacao() {
        when(notificacaoService.buscarUsuarioAutenticado(authentication))
                .thenReturn(Optional.empty());

        ResponseEntity<?> resposta =
                controller.buscar(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(preferenciaService);
    }

    @Test
    void deveBuscarPreferenciasDoUsuarioAutenticado() {
        autenticarUsuario(1L);

        PreferenciaNotificacao preferencia =
                mock(PreferenciaNotificacao.class);

        when(preferenciaService.buscarOuCriar(1L))
                .thenReturn(preferencia);

        ResponseEntity<?> resposta =
                controller.buscar(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());

        verify(preferenciaService).buscarOuCriar(1L);
    }

    @Test
    void deveBuscarPreferenciasDoUsuarioCorreto() {
        autenticarUsuario(25L);

        PreferenciaNotificacao preferencia =
                mock(PreferenciaNotificacao.class);

        when(preferenciaService.buscarOuCriar(25L))
                .thenReturn(preferencia);

        ResponseEntity<?> resposta =
                controller.buscar(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        verify(preferenciaService).buscarOuCriar(25L);
        verify(preferenciaService, never()).buscarOuCriar(1L);
    }

    @Test
    void deveNegarAtualizacaoSemAutenticacao() {
        PreferenciaNotificacaoDTO dados =
                mock(PreferenciaNotificacaoDTO.class);

        when(notificacaoService.buscarUsuarioAutenticado(authentication))
                .thenReturn(Optional.empty());

        ResponseEntity<?> resposta =
                controller.atualizar(authentication, dados);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(preferenciaService);
    }

    @Test
    void deveRejeitarAntecedenciaNegativa() {
        autenticarUsuario(1L);

        PreferenciaNotificacaoDTO dados =
                mock(PreferenciaNotificacaoDTO.class);

        when(dados.antecedenciaLembretes()).thenReturn(-1);

        ResponseEntity<?> resposta =
                controller.atualizar(authentication, dados);

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "A antecedência deve ser de 0, 1 ou 2 dias.");

        verifyNoInteractions(preferenciaService);
    }

    @Test
    void deveRejeitarAntecedenciaMaiorQueDois() {
        autenticarUsuario(1L);

        PreferenciaNotificacaoDTO dados =
                mock(PreferenciaNotificacaoDTO.class);

        when(dados.antecedenciaLembretes()).thenReturn(3);

        ResponseEntity<?> resposta =
                controller.atualizar(authentication, dados);

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "A antecedência deve ser de 0, 1 ou 2 dias.");

        verifyNoInteractions(preferenciaService);
    }

    @Test
    void deveAceitarAntecedenciaZero() {
        verificarAtualizacaoValida(0);
    }

    @Test
    void deveAceitarAntecedenciaUm() {
        verificarAtualizacaoValida(1);
    }

    @Test
    void deveAceitarAntecedenciaDois() {
        verificarAtualizacaoValida(2);
    }

    @Test
    void deveAtualizarPreferenciasDoUsuarioCorreto() {
        autenticarUsuario(25L);

        PreferenciaNotificacaoDTO dados =
                mock(PreferenciaNotificacaoDTO.class);

        PreferenciaNotificacao entidade =
                mock(PreferenciaNotificacao.class);

        when(dados.antecedenciaLembretes()).thenReturn(1);
        when(dados.toEntity()).thenReturn(entidade);

        when(preferenciaService.salvar(eq(25L), same(entidade)))
                .thenReturn(entidade);

        ResponseEntity<?> resposta =
                controller.atualizar(authentication, dados);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        verify(preferenciaService).salvar(25L, entidade);
    }

    private void verificarAtualizacaoValida(int antecedencia) {
        autenticarUsuario(1L);

        PreferenciaNotificacaoDTO dados =
                mock(PreferenciaNotificacaoDTO.class);

        PreferenciaNotificacao entidade =
                mock(PreferenciaNotificacao.class);

        when(dados.antecedenciaLembretes())
                .thenReturn(antecedencia);

        when(dados.toEntity()).thenReturn(entidade);

        when(preferenciaService.salvar(eq(1L), same(entidade)))
                .thenReturn(entidade);

        ResponseEntity<?> resposta =
                controller.atualizar(authentication, dados);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());

        verify(preferenciaService).salvar(1L, entidade);
    }

    private void autenticarUsuario(Long id) {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getId()).thenReturn(id);

        when(notificacaoService.buscarUsuarioAutenticado(authentication))
                .thenReturn(Optional.of(usuario));
    }

    private void assertErro(
            ResponseEntity<?> resposta,
            HttpStatus status,
            String mensagem) {

        assertEquals(status, resposta.getStatusCode());
        assertEquals(Map.of("erro", mensagem), resposta.getBody());
    }
}
