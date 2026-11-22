
package com.sap.controller;

import com.sap.model.Assunto;
import com.sap.model.Avaliacao;
import com.sap.model.DesempenhoAssunto;
import com.sap.model.Disciplina;
import com.sap.model.Resultado;
import com.sap.model.Usuario;
import com.sap.repository.DesempenhoAssuntoRepository;
import com.sap.repository.ResultadoRepository;
import com.sap.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResultadoControllerTest {

    private ResultadoRepository resultadoRepository;
    private UsuarioRepository usuarioRepository;
    private DesempenhoAssuntoRepository desempenhoAssuntoRepository;
    private ResultadoController controller;

    @BeforeEach
    void configurar() {
        resultadoRepository = mock(ResultadoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        desempenhoAssuntoRepository =
                mock(DesempenhoAssuntoRepository.class);

        controller = new ResultadoController(
                resultadoRepository,
                usuarioRepository,
                desempenhoAssuntoRepository
        );
    }

    @Test
    void deveNegarHistoricoSemAutenticacao() {
        ResponseEntity<?> resposta = controller.meuHistorico(null);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(usuarioRepository, resultadoRepository);
    }

    @Test
    void deveNegarHistoricoComAutenticacaoInvalida() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(false);

        ResponseEntity<?> resposta =
                controller.meuHistorico(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(usuarioRepository, resultadoRepository);
    }

    @Test
    void deveNegarHistoricoQuandoUsuarioNaoExiste() {
        Authentication authentication = autenticacaoLocal();

        ResponseEntity<?> resposta =
                controller.meuHistorico(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verify(usuarioRepository).findByEmail("estudante@email.com");
        verifyNoInteractions(resultadoRepository);
    }

    @Test
    void deveNegarHistoricoParaProfessor() {
        Authentication authentication = autenticarLocal("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.meuHistorico(authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a estudantes.");

        verifyNoInteractions(resultadoRepository);
    }

    @Test
    void deveNegarHistoricoParaAdministrador() {
        Authentication authentication = autenticarLocal("ADMIN");

        ResponseEntity<?> resposta =
                controller.meuHistorico(authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a estudantes.");

        verifyNoInteractions(resultadoRepository);
    }

    @Test
    void deveRetornarHistoricoVazio() {
        Authentication authentication = autenticarLocal("ESTUDANTE");

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of());

        ResponseEntity<?> resposta =
                controller.meuHistorico(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(List.of(), resposta.getBody());

        verify(resultadoRepository)
                .findByUsuarioIdOrderByDataRealizacaoDesc(1L);
    }

    @Test
    void deveRetornarHistoricoExistente() {
        Authentication authentication = autenticarLocal("ESTUDANTE");
        Resultado resultado = resultadoCompleto(10L);

        when(resultadoRepository.findByUsuarioIdOrderByDataRealizacaoDesc(1L))
                .thenReturn(List.of(resultado));

        ResponseEntity<?> resposta =
                controller.meuHistorico(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        List<?> lista = (List<?>) resposta.getBody();

        assertNotNull(lista);
        assertEquals(1, lista.size());

        Map<?, ?> dados = (Map<?, ?>) lista.get(0);

        assertEquals(10L, dados.get("resultadoId"));
        assertEquals(5L, dados.get("avaliacaoId"));
        assertEquals(3L, dados.get("disciplinaId"));
        assertEquals("Programação", dados.get("disciplinaNome"));
        assertNotNull(dados.get("totalQuestoes"));
        assertNotNull(dados.get("acertos"));
        assertNotNull(dados.get("erros"));
        assertNotNull(dados.get("percentual"));
        assertNotNull(dados.get("dataRealizacao"));
    }

    @Test
    void deveConsultarHistoricoDoUsuarioCorreto() {
        Authentication authentication = autenticarLocalComId("ESTUDANTE", 25L);

        controller.meuHistorico(authentication);

        verify(resultadoRepository)
                .findByUsuarioIdOrderByDataRealizacaoDesc(25L);
    }

    @Test
    void deveNegarDesempenhoSemAutenticacao() {
        ResponseEntity<?> resposta =
                controller.meuDesempenhoAssuntos(null);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(
                usuarioRepository,
                desempenhoAssuntoRepository
        );
    }

    @Test
    void deveNegarDesempenhoComAutenticacaoInvalida() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(false);

        ResponseEntity<?> resposta =
                controller.meuDesempenhoAssuntos(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(
                usuarioRepository,
                desempenhoAssuntoRepository
        );
    }

    @Test
    void deveNegarDesempenhoQuandoUsuarioNaoExiste() {
        Authentication authentication = autenticacaoLocal();

        ResponseEntity<?> resposta =
                controller.meuDesempenhoAssuntos(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verify(usuarioRepository).findByEmail("estudante@email.com");
        verifyNoInteractions(desempenhoAssuntoRepository);
    }

    @Test
    void deveNegarDesempenhoParaProfessor() {
        Authentication authentication = autenticarLocal("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.meuDesempenhoAssuntos(authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a estudantes.");

        verifyNoInteractions(desempenhoAssuntoRepository);
    }

    @Test
    void deveNegarDesempenhoParaAdministrador() {
        Authentication authentication = autenticarLocal("ADMIN");

        ResponseEntity<?> resposta =
                controller.meuDesempenhoAssuntos(authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a estudantes.");

        verifyNoInteractions(desempenhoAssuntoRepository);
    }

    @Test
    void deveRetornarDesempenhoVazio() {
        Authentication authentication = autenticarLocal("ESTUDANTE");

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of());

        ResponseEntity<?> resposta =
                controller.meuDesempenhoAssuntos(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(List.of(), resposta.getBody());
    }

    @Test
    void deveClassificarPrioridadeAlta() {
        verificarPrioridade("49.99", "ALTA");
    }

    @Test
    void deveClassificarPrioridadeAltaComZero() {
        verificarPrioridade("0", "ALTA");
    }

    @Test
    void deveClassificarPrioridadeMediaComCinquenta() {
        verificarPrioridade("50", "MEDIA");
    }

    @Test
    void deveClassificarPrioridadeMedia() {
        verificarPrioridade("65", "MEDIA");
    }

    @Test
    void deveClassificarPrioridadeMediaAbaixoDeSetenta() {
        verificarPrioridade("69.99", "MEDIA");
    }

    @Test
    void deveClassificarSemPrioridadeComSetenta() {
        verificarPrioridade("70", "SEM_PRIORIDADE");
    }

    @Test
    void deveClassificarSemPrioridadeComCem() {
        verificarPrioridade("100", "SEM_PRIORIDADE");
    }

    @Test
    void deveConsultarDesempenhoDoUsuarioCorreto() {
        Authentication authentication = autenticarLocalComId("ESTUDANTE", 25L);

        controller.meuDesempenhoAssuntos(authentication);

        verify(desempenhoAssuntoRepository)
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(25L);
    }

    private void verificarPrioridade(
            String percentual,
            String prioridadeEsperada) {

        Authentication authentication = autenticarLocal("ESTUDANTE");

        DesempenhoAssunto desempenho =
                desempenhoCompleto(new BigDecimal(percentual));

        when(desempenhoAssuntoRepository
                .findByResultado_UsuarioIdOrderByResultado_DataRealizacaoDesc(1L))
                .thenReturn(List.of(desempenho));

        ResponseEntity<?> resposta =
                controller.meuDesempenhoAssuntos(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        List<?> lista = (List<?>) resposta.getBody();

        assertNotNull(lista);
        assertEquals(1, lista.size());

        Map<?, ?> dados = (Map<?, ?>) lista.get(0);

        assertEquals(prioridadeEsperada, dados.get("prioridade"));
        assertEquals(new BigDecimal(percentual), dados.get("percentual"));
        assertEquals("Orientação a Objetos", dados.get("assuntoNome"));
        assertEquals("Programação", dados.get("disciplinaNome"));
    }

    private Resultado resultadoCompleto(Long id) {
        Resultado resultado = mock(Resultado.class);
        Avaliacao avaliacao = mock(Avaliacao.class);
        Assunto assunto = mock(Assunto.class);
        Disciplina disciplina = mock(Disciplina.class);

        when(resultado.getId()).thenReturn(id);
        when(resultado.getAvaliacao()).thenReturn(avaliacao);
        when(resultado.getTotalQuestoes()).thenReturn(10);
        when(resultado.getAcertos()).thenReturn(8);
        when(resultado.getErros()).thenReturn(2);
        when(resultado.getPercentual()).thenReturn(new BigDecimal("80"));
        when(resultado.getDataRealizacao())
                .thenReturn(LocalDateTime.of(2026, 11, 21, 14, 30));

        when(avaliacao.getId()).thenReturn(5L);
        when(avaliacao.getAssunto()).thenReturn(assunto);

        when(assunto.getDisciplina()).thenReturn(disciplina);

        when(disciplina.getId()).thenReturn(3L);
        when(disciplina.getNome()).thenReturn("Programação");

        return resultado;
    }

    private DesempenhoAssunto desempenhoCompleto(BigDecimal percentual) {
        DesempenhoAssunto desempenho = mock(DesempenhoAssunto.class);
        Resultado resultado = mock(Resultado.class);
        Avaliacao avaliacao = mock(Avaliacao.class);
        Assunto assunto = mock(Assunto.class);
        Disciplina disciplina = mock(Disciplina.class);

        when(desempenho.getPercentual()).thenReturn(percentual);
        when(desempenho.getResultado()).thenReturn(resultado);
        when(desempenho.getAssunto()).thenReturn(assunto);

        when(desempenho.getTotalQuestoes()).thenReturn(10);
        when(desempenho.getAcertos()).thenReturn(8);
        when(desempenho.getErros()).thenReturn(2);

        when(resultado.getId()).thenReturn(10L);
        when(resultado.getAvaliacao()).thenReturn(avaliacao);
        when(resultado.getDataRealizacao())
                .thenReturn(LocalDateTime.of(2026, 11, 21, 14, 30));

        when(avaliacao.getId()).thenReturn(5L);

        when(assunto.getId()).thenReturn(2L);
        when(assunto.getNome()).thenReturn("Orientação a Objetos");
        when(assunto.getDisciplina()).thenReturn(disciplina);

        when(disciplina.getId()).thenReturn(3L);
        when(disciplina.getNome()).thenReturn("Programação");

        return desempenho;
    }

    private Authentication autenticacaoLocal() {
        return new UsernamePasswordAuthenticationToken(
                "estudante@email.com",
                null,
                List.of()
        );
    }

    private Authentication autenticarLocal(String tipo) {
        return autenticarLocalComId(tipo, 1L);
    }

    private Authentication autenticarLocalComId(String tipo, Long id) {
        Authentication authentication = autenticacaoLocal();
        Usuario usuario = usuario(tipo, id);

        when(usuarioRepository.findByEmail("estudante@email.com"))
                .thenReturn(Optional.of(usuario));

        return authentication;
    }

    private Usuario usuario(String tipo, Long id) {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getTipo()).thenReturn(tipo);
        when(usuario.getId()).thenReturn(id);

        return usuario;
    }

    private void assertErro(
            ResponseEntity<?> resposta,
            HttpStatus status,
            String mensagem) {

        assertEquals(status, resposta.getStatusCode());
        assertEquals(Map.of("erro", mensagem), resposta.getBody());
    }
}
