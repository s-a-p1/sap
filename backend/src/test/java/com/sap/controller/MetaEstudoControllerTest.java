
package com.sap.controller;

import com.sap.model.Assunto;
import com.sap.model.Disciplina;
import com.sap.model.MetaEstudo;
import com.sap.model.ProvedorAutenticacao;
import com.sap.model.Usuario;
import com.sap.repository.UsuarioRepository;
import com.sap.service.MetaEstudoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetaEstudoControllerTest {

    private MetaEstudoService metaEstudoService;
    private UsuarioRepository usuarioRepository;
    private MetaEstudoController controller;

    @BeforeEach
    void configurar() {
        metaEstudoService = mock(MetaEstudoService.class);
        usuarioRepository = mock(UsuarioRepository.class);

        controller = new MetaEstudoController(
                metaEstudoService,
                usuarioRepository
        );
    }

    @Test
    void deveNegarListagemSemAutenticacao() {
        ResponseEntity<?> resposta = controller.listarMetas(null);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(usuarioRepository, metaEstudoService);
    }

    @Test
    void deveNegarListagemComAutenticacaoInvalida() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(false);

        ResponseEntity<?> resposta =
                controller.listarMetas(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");
    }

    @Test
    void deveNegarListagemQuandoUsuarioNaoExiste() {
        Authentication authentication = autenticacaoLocal();

        ResponseEntity<?> resposta =
                controller.listarMetas(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verify(usuarioRepository).findByEmail("estudante@email.com");
        verifyNoInteractions(metaEstudoService);
    }

    @Test
    void deveNegarListagemParaProfessor() {
        Authentication authentication = autenticarLocal("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.listarMetas(authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a estudantes.");

        verifyNoInteractions(metaEstudoService);
    }

    @Test
    void deveListarMetasVazias() {
        Authentication authentication = autenticarLocal("ESTUDANTE");

        when(metaEstudoService.listarMetas(1L))
                .thenReturn(List.of());

        ResponseEntity<?> resposta =
                controller.listarMetas(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(List.of(), resposta.getBody());

        verify(metaEstudoService).gerarMetasAutomaticamente(1L);
        verify(metaEstudoService).listarMetas(1L);
    }

    @Test
    void deveListarMetasExistentes() {
        Authentication authentication = autenticarLocal("ESTUDANTE");
        MetaEstudo meta = meta(10L, true);

        when(metaEstudoService.listarMetas(1L))
                .thenReturn(List.of(meta));

        ResponseEntity<?> resposta =
                controller.listarMetas(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        List<?> lista = (List<?>) resposta.getBody();

        assertNotNull(lista);
        assertEquals(1, lista.size());

        Map<?, ?> dados = (Map<?, ?>) lista.get(0);

        assertEquals(10L, dados.get("id"));
        assertEquals("Revisar Java", dados.get("titulo"));
        assertEquals("ALTA", dados.get("prioridade"));
        assertEquals(LocalDate.of(2026, 11, 25), dados.get("dataPrevista"));
        assertEquals(true, dados.get("concluida"));
        assertEquals(2L, dados.get("assuntoId"));
        assertEquals("Orientação a Objetos", dados.get("assuntoNome"));
        assertEquals(3L, dados.get("disciplinaId"));
        assertEquals("Programação", dados.get("disciplinaNome"));
    }

    @Test
    void deveGerarMetasAntesDeListar() {
        Authentication authentication = autenticarLocal("ESTUDANTE");

        controller.listarMetas(authentication);

        var ordem = inOrder(metaEstudoService);

        ordem.verify(metaEstudoService).gerarMetasAutomaticamente(1L);
        ordem.verify(metaEstudoService).listarMetas(1L);
    }

    @Test
    void deveNegarGeracaoSemAutenticacao() {
        ResponseEntity<?> resposta = controller.gerarMetas(null);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(metaEstudoService);
    }

    @Test
    void deveNegarGeracaoParaProfessor() {
        Authentication authentication = autenticarLocal("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.gerarMetas(authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a estudantes.");

        verifyNoInteractions(metaEstudoService);
    }

    @Test
    void deveGerarMetasComSucesso() {
        Authentication authentication = autenticarLocal("ESTUDANTE");

        ResponseEntity<?> resposta =
                controller.gerarMetas(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(
                Map.of("mensagem", "Metas geradas com sucesso."),
                resposta.getBody()
        );

        verify(metaEstudoService).gerarMetasAutomaticamente(1L);
    }

    @Test
    void deveNegarAtualizacaoSemAutenticacao() {
        ResponseEntity<?> resposta =
                controller.atualizarConclusao(
                        10L,
                        Map.of("concluida", true),
                        null
                );

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(metaEstudoService);
    }

    @Test
    void deveNegarAtualizacaoParaProfessor() {
        Authentication authentication = autenticarLocal("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.atualizarConclusao(
                        10L,
                        Map.of("concluida", true),
                        authentication
                );

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a estudantes.");
    }

    @Test
    void deveRejeitarAtualizacaoSemCampoConcluida() {
        Authentication authentication = autenticarLocal("ESTUDANTE");

        ResponseEntity<?> resposta =
                controller.atualizarConclusao(
                        10L,
                        Map.of(),
                        authentication
                );

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Informe o estado de conclusão da meta.");

        verify(metaEstudoService, never())
                .atualizarConclusao(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void deveMarcarMetaComoConcluida() {
        Authentication authentication = autenticarLocal("ESTUDANTE");
        MetaEstudo meta = meta(10L, true);

        when(metaEstudoService.atualizarConclusao(10L, 1L, true))
                .thenReturn(meta);

        ResponseEntity<?> resposta =
                controller.atualizarConclusao(
                        10L,
                        Map.of("concluida", true),
                        authentication
                );

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(
                Map.of("id", 10L, "concluida", true),
                resposta.getBody()
        );

        verify(metaEstudoService).atualizarConclusao(10L, 1L, true);
    }

    @Test
    void deveDesmarcarMetaComoConcluida() {
        Authentication authentication = autenticarLocal("ESTUDANTE");
        MetaEstudo meta = meta(10L, false);

        when(metaEstudoService.atualizarConclusao(10L, 1L, false))
                .thenReturn(meta);

        ResponseEntity<?> resposta =
                controller.atualizarConclusao(
                        10L,
                        Map.of("concluida", false),
                        authentication
                );

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(
                Map.of("id", 10L, "concluida", false),
                resposta.getBody()
        );
    }

    @Test
    void deveTratarErroAoAtualizarConclusao() {
        Authentication authentication = autenticarLocal("ESTUDANTE");

        when(metaEstudoService.atualizarConclusao(99L, 1L, true))
                .thenThrow(new IllegalArgumentException(
                        "Meta não encontrada."
                ));

        ResponseEntity<?> resposta =
                controller.atualizarConclusao(
                        99L,
                        Map.of("concluida", true),
                        authentication
                );

        assertErro(resposta, HttpStatus.BAD_REQUEST,
                "Meta não encontrada.");
    }

    @Test
    void deveAutenticarEstudantePeloGitHub() {
        Authentication authentication = autenticacaoOAuth(
                "github",
                Map.of("id", 12345)
        );

        Usuario usuario = usuario("ESTUDANTE");

        when(usuarioRepository.findByProvedorAndProvedorId(
                ProvedorAutenticacao.GITHUB, "12345"))
                .thenReturn(Optional.of(usuario));

        ResponseEntity<?> resposta =
                controller.gerarMetas(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        verify(usuarioRepository).findByProvedorAndProvedorId(
                ProvedorAutenticacao.GITHUB, "12345"
        );

        verify(metaEstudoService).gerarMetasAutomaticamente(1L);
    }

    @Test
    void deveAutenticarEstudantePeloGoogle() {
        Authentication authentication = autenticacaoOAuth(
                "google",
                Map.of("sub", "google-123")
        );

        Usuario usuario = usuario("ESTUDANTE");

        when(usuarioRepository.findByProvedorAndProvedorId(
                ProvedorAutenticacao.GOOGLE, "google-123"))
                .thenReturn(Optional.of(usuario));

        ResponseEntity<?> resposta =
                controller.gerarMetas(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        verify(usuarioRepository).findByProvedorAndProvedorId(
                ProvedorAutenticacao.GOOGLE, "google-123"
        );
    }

    @Test
    void deveNegarGitHubSemIdentificador() {
        Authentication authentication = autenticacaoOAuth(
                "github",
                Map.of("login", "usuario")
        );

        ResponseEntity<?> resposta =
                controller.gerarMetas(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(usuarioRepository, metaEstudoService);
    }

    @Test
    void deveNegarGoogleSemIdentificador() {
        Authentication authentication = autenticacaoOAuth(
                "google",
                Map.of("email", "usuario@gmail.com")
        );

        ResponseEntity<?> resposta =
                controller.gerarMetas(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(usuarioRepository, metaEstudoService);
    }

    @Test
    void deveNegarProvedorDesconhecido() {
        Authentication authentication = autenticacaoOAuth(
                "facebook",
                Map.of("id", "123")
        );

        ResponseEntity<?> resposta =
                controller.gerarMetas(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(usuarioRepository, metaEstudoService);
    }

    private Authentication autenticacaoLocal() {
        return new UsernamePasswordAuthenticationToken(
                "estudante@email.com",
                null,
                List.of()
        );
    }

    private Authentication autenticarLocal(String tipo) {
        Authentication authentication = autenticacaoLocal();
        Usuario usuario = usuario(tipo);

        when(usuarioRepository.findByEmail("estudante@email.com"))
                .thenReturn(Optional.of(usuario));

        return authentication;
    }

    private Usuario usuario(String tipo) {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getId()).thenReturn(1L);
        when(usuario.getTipo()).thenReturn(tipo);

        return usuario;
    }

    private Authentication autenticacaoOAuth(
            String provedor,
            Map<String, Object> atributos) {

        String atributoPrincipal =
                atributos.containsKey("id") ? "id" :
                atributos.containsKey("sub") ? "sub" :
                atributos.keySet().iterator().next();

        OAuth2User usuarioOAuth = new DefaultOAuth2User(
                List.of(() -> "ROLE_USER"),
                atributos,
                atributoPrincipal
        );

        return new OAuth2AuthenticationToken(
                usuarioOAuth,
                usuarioOAuth.getAuthorities(),
                provedor
        );
    }

    private MetaEstudo meta(Long id, boolean concluida) {
        MetaEstudo meta = mock(MetaEstudo.class);
        Assunto assunto = mock(Assunto.class);
        Disciplina disciplina = mock(Disciplina.class);

        when(meta.getId()).thenReturn(id);
        when(meta.getTitulo()).thenReturn("Revisar Java");
        when(meta.getPrioridade()).thenReturn("ALTA");
        when(meta.getDataPrevista())
                .thenReturn(LocalDate.of(2026, 11, 25));
        when(meta.getConcluida()).thenReturn(concluida);
        when(meta.getAssunto()).thenReturn(assunto);

        when(assunto.getId()).thenReturn(2L);
        when(assunto.getNome()).thenReturn("Orientação a Objetos");
        when(assunto.getDisciplina()).thenReturn(disciplina);

        when(disciplina.getId()).thenReturn(3L);
        when(disciplina.getNome()).thenReturn("Programação");

        return meta;
    }

    private void assertErro(
            ResponseEntity<?> resposta,
            HttpStatus status,
            String mensagem) {

        assertEquals(status, resposta.getStatusCode());
        assertEquals(Map.of("erro", mensagem), resposta.getBody());
    }
}
