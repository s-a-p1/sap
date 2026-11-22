
package com.sap.service;

import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioAutenticadoServiceTest {

    private UsuarioRepository usuarioRepository;
    private UsuarioAutenticadoService service;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        service = new UsuarioAutenticadoService(usuarioRepository);
    }

    @Test
    void deveRetornarVazioQuandoAutenticacaoForNula() {
        Optional<Usuario> resultado = service.buscar(null);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveRetornarVazioQuandoNaoEstiverAutenticado() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(false);

        Optional<Usuario> resultado = service.buscar(authentication);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveBuscarUsuarioAutenticadoPorEmail() {
        Authentication authentication = mock(Authentication.class);
        Usuario usuario = mock(Usuario.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("aluno@teste.com");

        when(usuarioRepository.findByEmail("aluno@teste.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = service.buscar(authentication);

        assertTrue(resultado.isPresent());
        assertSame(usuario, resultado.get());

        verify(usuarioRepository).findByEmail("aluno@teste.com");
    }

    @Test
    void deveRetornarVazioQuandoEmailNaoExistir() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("inexistente@teste.com");

        when(usuarioRepository.findByEmail("inexistente@teste.com"))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = service.buscar(authentication);

        assertTrue(resultado.isEmpty());

        verify(usuarioRepository).findByEmail("inexistente@teste.com");
    }

    @Test
    void deveBuscarUsuarioComEmailDiferente() {
        Authentication authentication = mock(Authentication.class);
        Usuario usuario = mock(Usuario.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("professor@teste.com");

        when(usuarioRepository.findByEmail("professor@teste.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = service.buscar(authentication);

        assertTrue(resultado.isPresent());
        assertSame(usuario, resultado.get());

        verify(usuarioRepository).findByEmail("professor@teste.com");
    }

    @Test
    void deveConsultarRepositorioUmaUnicaVez() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("aluno@teste.com");

        when(usuarioRepository.findByEmail("aluno@teste.com"))
                .thenReturn(Optional.empty());

        service.buscar(authentication);

        verify(usuarioRepository, times(1))
                .findByEmail("aluno@teste.com");

        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    void deveEncaminharNomeNuloAoRepositorio() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);

        when(usuarioRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = service.buscar(authentication);

        assertTrue(resultado.isEmpty());
        verify(usuarioRepository).findByEmail(null);
    }
}
