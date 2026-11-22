
package com.sap.service;

import com.sap.model.Notificacao;
import com.sap.model.Usuario;
import com.sap.repository.NotificacaoRepository;
import com.sap.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificacaoServiceTest {

    private NotificacaoRepository notificacaoRepository;
    private UsuarioRepository usuarioRepository;
    private NotificacaoService service;

    @BeforeEach
    void configurar() {
        notificacaoRepository = mock(NotificacaoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);

        service = new NotificacaoService(
                notificacaoRepository,
                usuarioRepository
        );
    }

    @Test
    void deveRetornarVazioQuandoAutenticacaoForNula() {
        Optional<Usuario> resultado = service.buscarUsuarioAutenticado(null);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveRetornarVazioQuandoUsuarioNaoEstiverAutenticado() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(false);

        Optional<Usuario> resultado =
                service.buscarUsuarioAutenticado(authentication);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveBuscarUsuarioAutenticadoPorEmail() {
        Authentication authentication = mock(Authentication.class);
        Usuario usuario = mock(Usuario.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("estudante@teste.com");

        when(usuarioRepository.findByEmail("estudante@teste.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                service.buscarUsuarioAutenticado(authentication);

        assertTrue(resultado.isPresent());
        assertSame(usuario, resultado.get());

        verify(usuarioRepository).findByEmail("estudante@teste.com");
    }

    @Test
    void deveRetornarVazioQuandoEmailNaoEstiverCadastrado() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("inexistente@teste.com");

        when(usuarioRepository.findByEmail("inexistente@teste.com"))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado =
                service.buscarUsuarioAutenticado(authentication);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveListarNotificacoesDoUsuario() {
        Notificacao primeira = new Notificacao();
        Notificacao segunda = new Notificacao();

        when(notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(primeira, segunda));

        List<Notificacao> resultado = service.listar(1L);

        assertEquals(2, resultado.size());
        assertSame(primeira, resultado.get(0));
        assertSame(segunda, resultado.get(1));
    }

    @Test
    void deveRetornarListaVaziaSemNotificacoes() {
        when(notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of());

        List<Notificacao> resultado = service.listar(1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveContarNotificacoesNaoLidas() {
        when(notificacaoRepository.countByUsuarioIdAndLidaFalse(1L))
                .thenReturn(5L);

        long quantidade = service.contarNaoLidas(1L);

        assertEquals(5L, quantidade);
    }

    @Test
    void deveRetornarZeroQuandoNaoExistiremNotificacoesNaoLidas() {
        when(notificacaoRepository.countByUsuarioIdAndLidaFalse(1L))
                .thenReturn(0L);

        assertEquals(0L, service.contarNaoLidas(1L));
    }

    @Test
    void deveMarcarNotificacaoComoLida() {
        Notificacao notificacao = new Notificacao();
        notificacao.setLida(false);

        when(notificacaoRepository.findByIdAndUsuarioId(10L, 1L))
                .thenReturn(Optional.of(notificacao));

        boolean resultado = service.marcarComoLida(10L, 1L);

        assertTrue(resultado);
        assertTrue(notificacao.isLida());

        verify(notificacaoRepository).save(notificacao);
    }

    @Test
    void deveRetornarFalseQuandoNotificacaoNaoExistir() {
        when(notificacaoRepository.findByIdAndUsuarioId(99L, 1L))
                .thenReturn(Optional.empty());

        boolean resultado = service.marcarComoLida(99L, 1L);

        assertFalse(resultado);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveMarcarNotificacaoDeOutroUsuario() {
        when(notificacaoRepository.findByIdAndUsuarioId(10L, 2L))
                .thenReturn(Optional.empty());

        boolean resultado = service.marcarComoLida(10L, 2L);

        assertFalse(resultado);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveMarcarComoLidaPeloMetodoAtualizarLeitura() {
        Notificacao notificacao = new Notificacao();
        notificacao.setLida(false);

        when(notificacaoRepository.findByIdAndUsuarioId(10L, 1L))
                .thenReturn(Optional.of(notificacao));

        boolean resultado = service.atualizarLeitura(10L, 1L, true);

        assertTrue(resultado);
        assertTrue(notificacao.isLida());

        verify(notificacaoRepository).save(notificacao);
    }

    @Test
    void deveMarcarComoNaoLida() {
        Notificacao notificacao = new Notificacao();
        notificacao.setLida(true);

        when(notificacaoRepository.findByIdAndUsuarioId(10L, 1L))
                .thenReturn(Optional.of(notificacao));

        boolean resultado = service.atualizarLeitura(10L, 1L, false);

        assertTrue(resultado);
        assertFalse(notificacao.isLida());

        verify(notificacaoRepository).save(notificacao);
    }

    @Test
    void deveRetornarFalseAoAtualizarNotificacaoInexistente() {
        when(notificacaoRepository.findByIdAndUsuarioId(99L, 1L))
                .thenReturn(Optional.empty());

        boolean resultado = service.atualizarLeitura(99L, 1L, true);

        assertFalse(resultado);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveAlterarNotificacaoDeOutroUsuario() {
        when(notificacaoRepository.findByIdAndUsuarioId(10L, 2L))
                .thenReturn(Optional.empty());

        boolean resultado = service.atualizarLeitura(10L, 2L, false);

        assertFalse(resultado);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void devePermitirMarcarNotificacaoJaLida() {
        Notificacao notificacao = new Notificacao();
        notificacao.setLida(true);

        when(notificacaoRepository.findByIdAndUsuarioId(10L, 1L))
                .thenReturn(Optional.of(notificacao));

        boolean resultado = service.marcarComoLida(10L, 1L);

        assertTrue(resultado);
        assertTrue(notificacao.isLida());

        verify(notificacaoRepository).save(notificacao);
    }
}
