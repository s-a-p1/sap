
package com.sap.controller;

import com.sap.dto.AvaliacaoRequest;
import com.sap.model.Assunto;
import com.sap.model.Avaliacao;
import com.sap.model.Disciplina;
import com.sap.model.Usuario;
import com.sap.repository.AssuntoRepository;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.QuestaoRepository;
import com.sap.repository.ResultadoRepository;
import com.sap.service.AvaliacaoService;
import com.sap.service.UsuarioAutenticadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvaliacaoControllerTest {

    private AvaliacaoService avaliacaoService;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private QuestaoRepository questaoRepository;
    private AssuntoRepository assuntoRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private ResultadoRepository resultadoRepository;
    private Authentication authentication;
    private AvaliacaoController controller;

    @BeforeEach
    void configurar() {
        avaliacaoService = mock(AvaliacaoService.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        questaoRepository = mock(QuestaoRepository.class);
        assuntoRepository = mock(AssuntoRepository.class);
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        resultadoRepository = mock(ResultadoRepository.class);
        authentication = mock(Authentication.class);

        controller = new AvaliacaoController(
                avaliacaoService,
                usuarioAutenticadoService,
                questaoRepository,
                assuntoRepository,
                avaliacaoRepository,
                resultadoRepository
        );
    }

    @Test
    void deveNegarCadastroSemAutenticacao() {
        semAutenticacao();

        ResponseEntity<?> resposta =
                controller.cadastrarProfessor(request(), authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");

        verifyNoInteractions(assuntoRepository, avaliacaoRepository);
    }

    @Test
    void deveNegarCadastroParaEstudante() {
        autenticar("ESTUDANTE");

        ResponseEntity<?> resposta =
                controller.cadastrarProfessor(request(), authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a professores.");
    }

    @Test
    void deveRetornarNaoEncontradoQuandoAssuntoNaoExisteNoCadastro() {
        autenticar("PROFESSOR");

        when(assuntoRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResponseEntity<?> resposta =
                controller.cadastrarProfessor(request(), authentication);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Assunto não encontrado.");
    }

    @Test
    void deveImpedirCadastroComTituloDuplicado() {
        autenticar("PROFESSOR");

        Assunto assunto = assunto();

        when(assuntoRepository.findById(2L))
                .thenReturn(Optional.of(assunto));

        when(avaliacaoRepository.existsByAssunto_IdAndTituloIgnoreCase(
                2L, "Prova Java"))
                .thenReturn(true);

        ResponseEntity<?> resposta =
                controller.cadastrarProfessor(request(), authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Já existe uma avaliação com este título neste assunto.");

        verify(avaliacaoRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveCadastrarAvaliacaoComSucesso() {
        autenticar("PROFESSOR");
        prepararAssunto();
        configurarSalvamento();

        ResponseEntity<?> resposta =
                controller.cadastrarProfessor(request(), authentication);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals(10L, dados.get("id"));
        assertEquals("Prova Java", dados.get("titulo"));
        assertEquals(2L, dados.get("assuntoId"));
        assertEquals(3L, dados.get("disciplinaId"));
        assertEquals("Programação", dados.get("disciplinaNome"));
        assertEquals(0, dados.get("quantidadeQuestoes"));

        verify(avaliacaoRepository).saveAndFlush(any(Avaliacao.class));
    }

    @Test
    void deveNormalizarTituloAoCadastrar() {
        autenticar("PROFESSOR");
        prepararAssunto();
        configurarSalvamento();

        controller.cadastrarProfessor(request(), authentication);

        verify(avaliacaoRepository).existsByAssunto_IdAndTituloIgnoreCase(
                2L, "Prova Java"
        );
    }

    @Test
    void deveTratarErroDeIntegridadeNoCadastro() {
        autenticar("PROFESSOR");
        prepararAssunto();

        when(avaliacaoRepository.saveAndFlush(any(Avaliacao.class)))
                .thenThrow(new DataIntegrityViolationException("Erro"));

        ResponseEntity<?> resposta =
                controller.cadastrarProfessor(request(), authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não foi possível cadastrar a avaliação.");
    }

    @Test
    void deveNegarEdicaoSemAutenticacao() {
        semAutenticacao();

        ResponseEntity<?> resposta =
                controller.editarProfessor(10L, request(), authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");
    }

    @Test
    void deveNegarEdicaoParaEstudante() {
        autenticar("ESTUDANTE");

        ResponseEntity<?> resposta =
                controller.editarProfessor(10L, request(), authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a professores.");
    }

    @Test
    void deveRetornarNaoEncontradoAoEditarAvaliacaoInexistente() {
        autenticar("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.editarProfessor(99L, request(), authentication);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Avaliação não encontrada.");
    }

    @Test
    void deveRetornarNaoEncontradoAoEditarComAssuntoInexistente() {
        autenticar("PROFESSOR");
        prepararAvaliacao();

        ResponseEntity<?> resposta =
                controller.editarProfessor(10L, request(), authentication);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Assunto não encontrado.");
    }

    @Test
    void deveImpedirAlteracaoDeAssuntoComQuestoes() {
        autenticar("PROFESSOR");
        prepararAvaliacao();

        Assunto novoAssunto = assuntoComId(5L);

        when(assuntoRepository.findById(5L))
                .thenReturn(Optional.of(novoAssunto));

        when(questaoRepository.countByAvaliacao_Id(10L))
                .thenReturn(3L);

        AvaliacaoRequest dados = request();
        dados.setAssuntoId(5L);

        ResponseEntity<?> resposta =
                controller.editarProfessor(10L, dados, authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível alterar o assunto de uma avaliação que possui questões cadastradas.");

        verify(avaliacaoRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveImpedirEdicaoComTituloDuplicado() {
        autenticar("PROFESSOR");
        prepararAvaliacao();
        prepararAssunto();

        when(avaliacaoRepository.existsByAssunto_IdAndTituloIgnoreCaseAndIdNot(
                2L, "Prova Java", 10L))
                .thenReturn(true);

        ResponseEntity<?> resposta =
                controller.editarProfessor(10L, request(), authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Já existe outra avaliação com este título neste assunto.");
    }

    @Test
    void deveEditarAvaliacaoComSucesso() {
        autenticar("PROFESSOR");
        prepararAvaliacao();
        prepararAssunto();

        when(questaoRepository.countByAvaliacao_Id(10L))
                .thenReturn(4L);

        when(avaliacaoRepository.saveAndFlush(any(Avaliacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        ResponseEntity<?> resposta =
                controller.editarProfessor(10L, request(), authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        Map<?, ?> dados = (Map<?, ?>) resposta.getBody();

        assertNotNull(dados);
        assertEquals("Prova Java", dados.get("titulo"));
        assertEquals(4L, dados.get("quantidadeQuestoes"));
    }

    @Test
    void deveTratarErroDeIntegridadeNaEdicao() {
        autenticar("PROFESSOR");
        prepararAvaliacao();
        prepararAssunto();

        when(avaliacaoRepository.saveAndFlush(any(Avaliacao.class)))
                .thenThrow(new DataIntegrityViolationException("Erro"));

        ResponseEntity<?> resposta =
                controller.editarProfessor(10L, request(), authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não foi possível atualizar a avaliação.");
    }

    @Test
    void deveNegarExclusaoSemAutenticacao() {
        semAutenticacao();

        ResponseEntity<?> resposta =
                controller.excluirProfessor(10L, authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");
    }

    @Test
    void deveNegarExclusaoParaEstudante() {
        autenticar("ESTUDANTE");

        ResponseEntity<?> resposta =
                controller.excluirProfessor(10L, authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a professores.");
    }

    @Test
    void deveRetornarNaoEncontradoAoExcluirAvaliacaoInexistente() {
        autenticar("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.excluirProfessor(99L, authentication);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Avaliação não encontrada.");
    }

    @Test
    void deveImpedirExclusaoComQuestoes() {
        autenticar("PROFESSOR");
        prepararAvaliacao();

        when(questaoRepository.countByAvaliacao_Id(10L))
                .thenReturn(2L);

        ResponseEntity<?> resposta =
                controller.excluirProfessor(10L, authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível excluir uma avaliação que possui questões cadastradas.");
    }

    @Test
    void deveImpedirExclusaoComResultados() {
        autenticar("PROFESSOR");
        prepararAvaliacao();

        when(resultadoRepository.existsByAvaliacao_Id(10L))
                .thenReturn(true);

        ResponseEntity<?> resposta =
                controller.excluirProfessor(10L, authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível excluir uma avaliação que possui resultados de estudantes.");
    }

    @Test
    void deveExcluirAvaliacaoComSucesso() {
        autenticar("PROFESSOR");
        prepararAvaliacao();

        ResponseEntity<?> resposta =
                controller.excluirProfessor(10L, authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        verify(avaliacaoRepository).delete(any(Avaliacao.class));
        verify(avaliacaoRepository).flush();

        assertEquals(
                Map.of("mensagem", "Avaliação excluída com sucesso."),
                resposta.getBody()
        );
    }

    @Test
    void deveTratarErroDeIntegridadeNaExclusao() {
        autenticar("PROFESSOR");
        prepararAvaliacao();

        doThrow(new DataIntegrityViolationException("Erro"))
                .when(avaliacaoRepository).flush();

        ResponseEntity<?> resposta =
                controller.excluirProfessor(10L, authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não foi possível excluir a avaliação porque existem registros vinculados.");
    }

    @Test
    void deveNegarListagemProfessorSemAutenticacao() {
        semAutenticacao();

        ResponseEntity<?> resposta =
                controller.listarProfessor(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");
    }

    @Test
    void deveNegarListagemProfessorParaEstudante() {
        autenticar("ESTUDANTE");

        ResponseEntity<?> resposta =
                controller.listarProfessor(authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a professores.");
    }

    @Test
    void deveListarAvaliacoesDoProfessor() {
        autenticar("PROFESSOR");

        Avaliacao avaliacao = avaliacao();

        when(avaliacaoService.listarTodas())
                .thenReturn(List.of(avaliacao));

        when(questaoRepository.countByAvaliacao_Id(10L))
                .thenReturn(5L);

        ResponseEntity<?> resposta =
                controller.listarProfessor(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        List<?> lista = (List<?>) resposta.getBody();

        assertNotNull(lista);
        assertEquals(1, lista.size());

        Map<?, ?> dados = (Map<?, ?>) lista.get(0);

        assertEquals(10L, dados.get("id"));
        assertEquals(5L, dados.get("quantidadeQuestoes"));
    }

    @Test
    void deveNegarListagemEstudanteSemAutenticacao() {
        semAutenticacao();

        ResponseEntity<?> resposta =
                controller.listar(authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");
    }

    @Test
    void deveNegarListagemEstudanteParaProfessor() {
        autenticar("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.listar(authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a estudantes.");
    }

    @Test
    void deveListarAvaliacoesDoEstudante() {
        Usuario estudante = autenticar("ESTUDANTE");
        Avaliacao avaliacao = avaliacao();

        when(avaliacaoService.listarDoEstudante(estudante))
                .thenReturn(List.of(avaliacao));

        ResponseEntity<?> resposta =
                controller.listar(authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        List<?> lista = (List<?>) resposta.getBody();

        assertNotNull(lista);
        assertEquals(1, lista.size());

        Map<?, ?> dados = (Map<?, ?>) lista.get(0);

        assertEquals(10L, dados.get("id"));
        assertEquals("Prova Java", dados.get("titulo"));
        assertEquals(3L, dados.get("disciplinaId"));
    }

    private Usuario autenticar(String tipo) {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getTipo()).thenReturn(tipo);

        when(usuarioAutenticadoService.buscar(authentication))
                .thenReturn(Optional.of(usuario));

        return usuario;
    }

    private void semAutenticacao() {
        when(usuarioAutenticadoService.buscar(authentication))
                .thenReturn(Optional.empty());
    }

    private AvaliacaoRequest request() {
        AvaliacaoRequest request = new AvaliacaoRequest();

        request.setTitulo("  Prova Java  ");
        request.setAssuntoId(2L);

        return request;
    }

    private Disciplina disciplina() {
        Disciplina disciplina = mock(Disciplina.class);

        when(disciplina.getId()).thenReturn(3L);
        when(disciplina.getNome()).thenReturn("Programação");

        return disciplina;
    }

    private Assunto assuntoComId(Long id) {
        Assunto assunto = mock(Assunto.class);
        Disciplina disciplina = disciplina();

        when(assunto.getId()).thenReturn(id);
        when(assunto.getDisciplina()).thenReturn(disciplina);

        return assunto;
    }

    private Assunto assunto() {
        return assuntoComId(2L);
    }

    private Avaliacao avaliacao() {
        Avaliacao avaliacao = new Avaliacao();

        avaliacao.setId(10L);
        avaliacao.setTitulo("Prova Java");
        avaliacao.setAssunto(assunto());
        avaliacao.setDataCriacao(
                LocalDateTime.of(2026, 11, 21, 10, 0)
        );

        return avaliacao;
    }

    private void prepararAssunto() {
        Assunto assunto = assunto();

        when(assuntoRepository.findById(2L))
                .thenReturn(Optional.of(assunto));
    }

    private void prepararAvaliacao() {
        Avaliacao avaliacao = avaliacao();

        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));
    }

    private void configurarSalvamento() {
        when(avaliacaoRepository.saveAndFlush(any(Avaliacao.class)))
                .thenAnswer(invocacao -> {
                    Avaliacao avaliacao = invocacao.getArgument(0);

                    avaliacao.setId(10L);
                    avaliacao.setDataCriacao(
                            LocalDateTime.of(2026, 11, 21, 10, 0)
                    );

                    return avaliacao;
                });
    }

    private void assertErro(
            ResponseEntity<?> resposta,
            HttpStatus status,
            String mensagem) {

        assertEquals(status, resposta.getStatusCode());

        assertEquals(
                Map.of("erro", mensagem),
                resposta.getBody()
        );
    }
}
