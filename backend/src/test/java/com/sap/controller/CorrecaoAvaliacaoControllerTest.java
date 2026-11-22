
package com.sap.controller;

import com.sap.dto.FinalizarAvaliacaoRequest;
import com.sap.dto.ResultadoAvaliacaoResponse;
import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import com.sap.service.CorrecaoAvaliacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
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
    }

    @Test
    void deveFinalizarAvaliacaoComGitHub() {
        Authentication authentication = autenticacaoOAuth(
                "github",
                Map.of("id", 12345)
        );

        Usuario usuario = mock(Usuario.class);
        ResultadoAvaliacaoResponse resultado = mock(ResultadoAvaliacaoResponse.class);

        when(usuarioRepository.findByProvedorAndProvedorId(
                ProvedorAutenticacao.GITHUB, "12345"))
                .thenReturn(Optional.of(usuario));

        when(correcaoAvaliacaoService.corrigir(request, usuario))
                .thenReturn(resultado);

        ResultadoAvaliacaoResponse resposta =
                controller.finalizar(request, authentication);

        assertSame(resultado, resposta);

        verify(usuarioRepository).findByProvedorAndProvedorId(
                ProvedorAutenticacao.GITHUB, "12345"
        );
    }

    @Test
    void deveAceitarGitHubComNomeMaiusculo() {
        Authentication authentication = autenticacaoOAuth(
                "GITHUB",
                Map.of("id", "98765")
        );

        Usuario usuario = mock(Usuario.class);
        ResultadoAvaliacaoResponse resultado = mock(ResultadoAvaliacaoResponse.class);

        when(usuarioRepository.findByProvedorAndProvedorId(
                ProvedorAutenticacao.GITHUB, "98765"))
                .thenReturn(Optional.of(usuario));

        when(correcaoAvaliacaoService.corrigir(request, usuario))
                .thenReturn(resultado);

        assertSame(
                resultado,
                controller.finalizar(request, authentication)
        );
    }

    @Test
    void deveNegarGitHubSemIdentificador() {
        Authentication authentication = autenticacaoOAuth(
                "github",
                Map.of("login", "usuario")
        );

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());

        verifyNoInteractions(usuarioRepository, correcaoAvaliacaoService);
    }

    @Test
    void deveNegarGitHubQuandoUsuarioNaoExiste() {
        Authentication authentication = autenticacaoOAuth(
                "github",
                Map.of("id", 12345)
        );

        when(usuarioRepository.findByProvedorAndProvedorId(
                ProvedorAutenticacao.GITHUB, "12345"))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());

        verifyNoInteractions(correcaoAvaliacaoService);
    }

    @Test
    void deveFinalizarAvaliacaoComGoogle() {
        Authentication authentication = autenticacaoOAuth(
                "google",
                Map.of("sub", "google-123")
        );

        Usuario usuario = mock(Usuario.class);
        ResultadoAvaliacaoResponse resultado = mock(ResultadoAvaliacaoResponse.class);

        when(usuarioRepository.findByProvedorAndProvedorId(
                ProvedorAutenticacao.GOOGLE, "google-123"))
                .thenReturn(Optional.of(usuario));

        when(correcaoAvaliacaoService.corrigir(request, usuario))
                .thenReturn(resultado);

        ResultadoAvaliacaoResponse resposta =
                controller.finalizar(request, authentication);

        assertSame(resultado, resposta);

        verify(usuarioRepository).findByProvedorAndProvedorId(
                ProvedorAutenticacao.GOOGLE, "google-123"
        );
    }

    @Test
    void deveAceitarGoogleComNomeMaiusculo() {
        Authentication authentication = autenticacaoOAuth(
                "GOOGLE",
                Map.of("sub", "google-456")
        );

        Usuario usuario = mock(Usuario.class);
        ResultadoAvaliacaoResponse resultado = mock(ResultadoAvaliacaoResponse.class);

        when(usuarioRepository.findByProvedorAndProvedorId(
                ProvedorAutenticacao.GOOGLE, "google-456"))
                .thenReturn(Optional.of(usuario));

        when(correcaoAvaliacaoService.corrigir(request, usuario))
                .thenReturn(resultado);

        assertSame(
                resultado,
                controller.finalizar(request, authentication)
        );
    }

    @Test
    void deveNegarGoogleSemIdentificador() {
        Authentication authentication = autenticacaoOAuth(
                "google",
                Map.of("email", "usuario@gmail.com")
        );

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());

        verifyNoInteractions(usuarioRepository, correcaoAvaliacaoService);
    }

    @Test
    void deveNegarGoogleQuandoUsuarioNaoExiste() {
        Authentication authentication = autenticacaoOAuth(
                "google",
                Map.of("sub", "google-123")
        );

        when(usuarioRepository.findByProvedorAndProvedorId(
                ProvedorAutenticacao.GOOGLE, "google-123"))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());

        verifyNoInteractions(correcaoAvaliacaoService);
    }

    @Test
    void deveNegarProvedorOAuthDesconhecido() {
        Authentication authentication = autenticacaoOAuth(
                "facebook",
                Map.of("id", "123")
        );

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(request, authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());

        verifyNoInteractions(usuarioRepository, correcaoAvaliacaoService);
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
    }

    private Authentication autenticacaoLocal(String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of()
        );
    }

    private Authentication autenticacaoOAuth(
            String provedor,
            Map<String, Object> atributos) {

        OAuth2User usuarioOAuth = new DefaultOAuth2User(
                List.of(() -> "ROLE_USER"),
                atributos,
                atributos.containsKey("id") ? "id" :
                        atributos.containsKey("sub") ? "sub" :
                        atributos.keySet().iterator().next()
        );

        return new OAuth2AuthenticationToken(
                usuarioOAuth,
                usuarioOAuth.getAuthorities(),
                provedor
        );
    }
}
