
package com.sap.controller;

import com.sap.dto.FinalizarAvaliacaoRequest;
import com.sap.dto.ResultadoAvaliacaoResponse;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import com.sap.service.CorrecaoAvaliacaoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorrecaoAvaliacaoControllerTest {

    private CorrecaoAvaliacaoService correcaoAvaliacaoService;
    private UsuarioRepository usuarioRepository;
    private CorrecaoAvaliacaoController controller;
    private FinalizarAvaliacaoRequest request;

    @BeforeEach
    void configurar() {
        correcaoAvaliacaoService = mock(CorrecaoAvaliacaoService.class);
        usuarioRepository = mock(UsuarioRepository.class);

        controller = new CorrecaoAvaliacaoController(
                correcaoAvaliacaoService,
                usuarioRepository
        );

        request = mock(FinalizarAvaliacaoRequest.class);
    }

    @Test
    void deveNegarFinalizacaoSemAutenticacao() {
        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(request, null)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());
        assertEquals("Usuário não autenticado.", erro.getReason());

        verifyNoInteractions(usuarioRepository, correcaoAvaliacaoService);
    }

    @Test
    void deveNegarFinalizacaoComAutenticacaoInvalida() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(false);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());

        verifyNoInteractions(usuarioRepository, correcaoAvaliacaoService);
    }

    @Test
    void deveNegarFinalizacaoQuandoUsuarioLocalNaoExiste() {
        Authentication authentication = autenticacaoLocal("teste@email.com");

        when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());

        verify(usuarioRepository).findByEmail("teste@email.com");
        verifyNoInteractions(correcaoAvaliacaoService);
    }

    @Test
    void deveFinalizarAvaliacaoComUsuarioLocal() {
        Authentication authentication = autenticacaoLocal("teste@email.com");
        Usuario usuario = mock(Usuario.class);
        ResultadoAvaliacaoResponse resultado = mock(ResultadoAvaliacaoResponse.class);

        when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));

        when(correcaoAvaliacaoService.corrigir(request, usuario))
                .thenReturn(resultado);

        ResultadoAvaliacaoResponse resposta =
                controller.finalizar(request, authentication);

        assertSame(resultado, resposta);

        verify(usuarioRepository).findByEmail("teste@email.com");
        verify(correcaoAvaliacaoService).corrigir(request, usuario);
    }

    @Test
    void deveNormalizarEmailDoUsuarioLocal() {
        Authentication authentication = autenticacaoLocal("  TESTE@EMAIL.COM  ");
        Usuario usuario = mock(Usuario.class);
        ResultadoAvaliacaoResponse resultado = mock(ResultadoAvaliacaoResponse.class);

        when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));

        when(correcaoAvaliacaoService.corrigir(request, usuario))
                .thenReturn(resultado);

        ResultadoAvaliacaoResponse resposta =
                controller.finalizar(request, authentication);

        assertSame(resultado, resposta);

        verify(usuarioRepository).findByEmail("teste@email.com");
        verify(correcaoAvaliacaoService).corrigir(request, usuario);
    }

    @Test
    void deveAceitarEmailComLetrasMaiusculas() {
        Authentication authentication = autenticacaoLocal("USUARIO@EMAIL.COM");
        Usuario usuario = mock(Usuario.class);
        ResultadoAvaliacaoResponse resultado = mock(ResultadoAvaliacaoResponse.class);

        when(usuarioRepository.findByEmail("usuario@email.com"))
                .thenReturn(Optional.of(usuario));

        when(correcaoAvaliacaoService.corrigir(request, usuario))
                .thenReturn(resultado);

        ResultadoAvaliacaoResponse resposta =
                controller.finalizar(request, authentication);

        assertSame(resultado, resposta);

        verify(usuarioRepository).findByEmail("usuario@email.com");
    }

    @Test
    void deveNegarUsuarioNaoEncontradoComEmailMaiusculo() {
        Authentication authentication = autenticacaoLocal("INEXISTENTE@EMAIL.COM");

        when(usuarioRepository.findByEmail("inexistente@email.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());

        verify(usuarioRepository).findByEmail("inexistente@email.com");
        verifyNoInteractions(correcaoAvaliacaoService);
    }

    @Test
    void devePropagarErroDoService() {
        Authentication authentication = autenticacaoLocal("teste@email.com");
        Usuario usuario = mock(Usuario.class);

        when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));

        when(correcaoAvaliacaoService.corrigir(request, usuario))
                .thenThrow(new IllegalArgumentException(
                        "Avaliação inválida."
                ));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals("Avaliação inválida.", erro.getMessage());

        verify(correcaoAvaliacaoService).corrigir(request, usuario);
    }

    @Test
    void devePropagarErroDeProcessamentoDaAvaliacao() {
        Authentication authentication = autenticacaoLocal("teste@email.com");
        Usuario usuario = mock(Usuario.class);

        when(usuarioRepository.findByEmail("teste@email.com"))
                .thenReturn(Optional.of(usuario));

        when(correcaoAvaliacaoService.corrigir(request, usuario))
                .thenThrow(new IllegalStateException(
                        "Não foi possível processar a avaliação."
                ));

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals(
                "Não foi possível processar a avaliação.",
                erro.getMessage()
        );

        verify(correcaoAvaliacaoService).corrigir(request, usuario);
    }

    private Authentication autenticacaoLocal(String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of()
        );
    }
}
