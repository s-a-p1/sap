
package com.sap.controller;

import com.sap.dto.QuestaoEdicaoRequest;
import com.sap.dto.QuestaoRequest;
import com.sap.dto.QuestaoResponse;
import com.sap.model.Avaliacao;
import com.sap.model.Questao;
import com.sap.model.Usuario;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.QuestaoRepository;
import com.sap.repository.ResultadoRepository;
import com.sap.service.QuestaoService;
import com.sap.service.UsuarioAutenticadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QuestaoControllerTest {

    private QuestaoService questaoService;
    private QuestaoRepository questaoRepository;
    private ResultadoRepository resultadoRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private QuestaoController controller;
    private Authentication authentication;

    @BeforeEach
    void configurar() {
        questaoService = mock(QuestaoService.class);
        questaoRepository = mock(QuestaoRepository.class);
        resultadoRepository = mock(ResultadoRepository.class);
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        authentication = mock(Authentication.class);

        controller = new QuestaoController(
                questaoService,
                questaoRepository,
                resultadoRepository,
                avaliacaoRepository,
                usuarioAutenticadoService
        );
    }

    @Test
    void deveListarQuestoesPorAvaliacao() {
        QuestaoResponse primeira = mock(QuestaoResponse.class);
        QuestaoResponse segunda = mock(QuestaoResponse.class);

        when(questaoService.listarPorAvaliacao(5L))
                .thenReturn(List.of(primeira, segunda));

        List<QuestaoResponse> resposta =
                controller.listarPorAvaliacao(5L);

        assertEquals(2, resposta.size());
        assertSame(primeira, resposta.get(0));
        assertSame(segunda, resposta.get(1));

        verify(questaoService).listarPorAvaliacao(5L);
    }

    @Test
    void deveRetornarListaVaziaDeQuestoes() {
        when(questaoService.listarPorAvaliacao(5L))
                .thenReturn(List.of());

        assertTrue(controller.listarPorAvaliacao(5L).isEmpty());
    }

    @Test
    void deveNegarListagemProfessorSemAutenticacao() {
        ResponseEntity<?> resposta =
                controller.listarProfessor(5L, authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");
    }

    @Test
    void deveNegarListagemProfessorParaEstudante() {
        autenticar("ESTUDANTE");

        ResponseEntity<?> resposta =
                controller.listarProfessor(5L, authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a professores.");
    }

    @Test
    void deveRetornarAvaliacaoNaoEncontradaNaListagem() {
        autenticar("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.listarProfessor(99L, authentication);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Avaliação não encontrada.");
    }

    @Test
    void deveListarQuestoesDoProfessor() {
        autenticar("PROFESSOR");
        prepararAvaliacao(5L);

        Questao questao = questaoCompleta(10L);

        when(questaoRepository.findByAvaliacao_Id(5L))
                .thenReturn(List.of(questao));

        ResponseEntity<?> resposta =
                controller.listarProfessor(5L, authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        List<?> lista = (List<?>) resposta.getBody();

        assertNotNull(lista);
        assertEquals(1, lista.size());

        Map<?, ?> dados = (Map<?, ?>) lista.get(0);

        assertEquals(10L, dados.get("id"));
        assertEquals("Qual é a resposta?", dados.get("enunciado"));
        assertEquals("A", dados.get("respostaCorreta"));
    }

    @Test
    void deveListarQuestoesVaziasDoProfessor() {
        autenticar("PROFESSOR");
        prepararAvaliacao(5L);

        ResponseEntity<?> resposta =
                controller.listarProfessor(5L, authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(List.of(), resposta.getBody());
    }

    @Test
    void deveNegarCadastroSemAutenticacao() {
        QuestaoRequest dados = mock(QuestaoRequest.class);

        ResponseEntity<?> resposta =
                controller.cadastrarQuestaoProfessor(dados, authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");
    }

    @Test
    void deveNegarCadastroParaEstudante() {
        autenticar("ESTUDANTE");

        QuestaoRequest dados = mock(QuestaoRequest.class);

        ResponseEntity<?> resposta =
                controller.cadastrarQuestaoProfessor(dados, authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a professores.");
    }

    @Test
    void deveRejeitarCadastroEmAvaliacaoInexistente() {
        autenticar("PROFESSOR");

        QuestaoRequest dados = mock(QuestaoRequest.class);

        when(dados.getAvaliacaoId()).thenReturn(99L);

        ResponseEntity<?> resposta =
                controller.cadastrarQuestaoProfessor(dados, authentication);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Avaliação não encontrada.");
    }

    @Test
    void deveImpedirCadastroQuandoExistemResultados() {
        autenticar("PROFESSOR");
        prepararAvaliacao(5L);

        QuestaoRequest dados = mock(QuestaoRequest.class);

        when(dados.getAvaliacaoId()).thenReturn(5L);
        when(resultadoRepository.existsByAvaliacao_Id(5L))
                .thenReturn(true);

        ResponseEntity<?> resposta =
                controller.cadastrarQuestaoProfessor(dados, authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Não é possível adicionar questões porque esta avaliação já possui resultados de estudantes.");

        verify(questaoRepository, never()).save(any());
    }

    @Test
    void deveCadastrarQuestaoComSucesso() {
        autenticar("PROFESSOR");
        prepararAvaliacao(5L);

        QuestaoRequest dados = dadosCadastro();

        Questao salva = mock(Questao.class);

        when(salva.getId()).thenReturn(10L);
        when(questaoRepository.save(any(Questao.class)))
                .thenReturn(salva);

        ResponseEntity<?> resposta =
                controller.cadastrarQuestaoProfessor(dados, authentication);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals(
                Map.of(
                        "id", 10L,
                        "mensagem", "Questão cadastrada com sucesso."
                ),
                resposta.getBody()
        );

        verify(questaoRepository).save(any(Questao.class));
    }

    @Test
    void deveRemoverEspacosAoCadastrarQuestao() {
        autenticar("PROFESSOR");
        prepararAvaliacao(5L);

        QuestaoRequest dados = dadosCadastro();
        Questao salva = mock(Questao.class);

        when(salva.getId()).thenReturn(10L);
        when(questaoRepository.save(any(Questao.class)))
                .thenReturn(salva);

        controller.cadastrarQuestaoProfessor(dados, authentication);

        ArgumentCaptor<Questao> captor =
                ArgumentCaptor.forClass(Questao.class);

        verify(questaoRepository).save(captor.capture());

        Questao questao = captor.getValue();

        assertEquals("Qual é a resposta?", questao.getEnunciado());
        assertEquals("Alternativa A", questao.getAlternativaA());
        assertEquals("A", questao.getRespostaCorreta());
    }

    @Test
    void deveNegarEdicaoSemAutenticacao() {
        QuestaoEdicaoRequest dados = mock(QuestaoEdicaoRequest.class);

        ResponseEntity<?> resposta =
                controller.editarQuestaoProfessor(
                        10L, dados, authentication
                );

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");
    }

    @Test
    void deveNegarEdicaoParaEstudante() {
        autenticar("ESTUDANTE");

        QuestaoEdicaoRequest dados = mock(QuestaoEdicaoRequest.class);

        ResponseEntity<?> resposta =
                controller.editarQuestaoProfessor(
                        10L, dados, authentication
                );

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a professores.");
    }

    @Test
    void deveRejeitarEdicaoDeQuestaoInexistente() {
        autenticar("PROFESSOR");

        QuestaoEdicaoRequest dados = mock(QuestaoEdicaoRequest.class);

        ResponseEntity<?> resposta =
                controller.editarQuestaoProfessor(
                        99L, dados, authentication
                );

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Questão não encontrada.");
    }

    @Test
    void deveImpedirEdicaoQuandoExistemResultados() {
        autenticar("PROFESSOR");

        prepararQuestaoExistente(10L, 5L);

        when(resultadoRepository.existsByAvaliacao_Id(5L))
                .thenReturn(true);

        QuestaoEdicaoRequest dados = mock(QuestaoEdicaoRequest.class);

        ResponseEntity<?> resposta =
                controller.editarQuestaoProfessor(
                        10L, dados, authentication
                );

        assertErro(resposta, HttpStatus.CONFLICT,
                "Esta questão não pode ser editada porque a avaliação já possui resultados de estudantes.");

        verify(questaoRepository, never()).save(any());
    }

    @Test
    void deveEditarQuestaoComSucesso() {
        autenticar("PROFESSOR");

        Questao questao = prepararQuestaoExistente(10L, 5L);
        QuestaoEdicaoRequest dados = dadosEdicao();

        ResponseEntity<?> resposta =
                controller.editarQuestaoProfessor(
                        10L, dados, authentication
                );

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(
                Map.of("mensagem", "Questão atualizada com sucesso."),
                resposta.getBody()
        );

        verify(questao).setEnunciado("Enunciado atualizado");
        verify(questao).setAlternativaA("Nova alternativa A");
        verify(questao).setRespostaCorreta("B");
        verify(questaoRepository).save(questao);
    }

    @Test
    void deveNegarExclusaoSemAutenticacao() {
        ResponseEntity<?> resposta =
                controller.excluirQuestaoProfessor(10L, authentication);

        assertErro(resposta, HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado.");
    }

    @Test
    void deveNegarExclusaoParaEstudante() {
        autenticar("ESTUDANTE");

        ResponseEntity<?> resposta =
                controller.excluirQuestaoProfessor(10L, authentication);

        assertErro(resposta, HttpStatus.FORBIDDEN,
                "Acesso permitido apenas a professores.");
    }

    @Test
    void deveRejeitarExclusaoDeQuestaoInexistente() {
        autenticar("PROFESSOR");

        ResponseEntity<?> resposta =
                controller.excluirQuestaoProfessor(99L, authentication);

        assertErro(resposta, HttpStatus.NOT_FOUND,
                "Questão não encontrada.");
    }

    @Test
    void deveImpedirExclusaoQuandoExistemResultados() {
        autenticar("PROFESSOR");

        prepararQuestaoExistente(10L, 5L);

        when(resultadoRepository.existsByAvaliacao_Id(5L))
                .thenReturn(true);

        ResponseEntity<?> resposta =
                controller.excluirQuestaoProfessor(10L, authentication);

        assertErro(resposta, HttpStatus.CONFLICT,
                "Esta questão não pode ser excluída porque a avaliação já possui resultados de estudantes.");

        verify(questaoRepository, never()).delete(any());
    }

    @Test
    void deveExcluirQuestaoComSucesso() {
        autenticar("PROFESSOR");

        Questao questao = prepararQuestaoExistente(10L, 5L);

        ResponseEntity<?> resposta =
                controller.excluirQuestaoProfessor(10L, authentication);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(
                Map.of("mensagem", "Questão excluída com sucesso."),
                resposta.getBody()
        );

        verify(questaoRepository).delete(questao);
    }

    private void autenticar(String tipo) {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getTipo()).thenReturn(tipo);

        when(usuarioAutenticadoService.buscar(authentication))
                .thenReturn(Optional.of(usuario));
    }

    private Avaliacao prepararAvaliacao(Long id) {
        Avaliacao avaliacao = mock(Avaliacao.class);

        when(avaliacaoRepository.findById(id))
                .thenReturn(Optional.of(avaliacao));

        return avaliacao;
    }

    private Questao prepararQuestaoExistente(
            Long questaoId,
            Long avaliacaoId) {

        Questao questao = mock(Questao.class);
        Avaliacao avaliacao = mock(Avaliacao.class);

        when(avaliacao.getId()).thenReturn(avaliacaoId);
        when(questao.getAvaliacao()).thenReturn(avaliacao);

        when(questaoRepository.findById(questaoId))
                .thenReturn(Optional.of(questao));

        return questao;
    }

    private Questao questaoCompleta(Long id) {
        Questao questao = mock(Questao.class);

        when(questao.getId()).thenReturn(id);
        when(questao.getEnunciado()).thenReturn("Qual é a resposta?");
        when(questao.getAlternativaA()).thenReturn("Alternativa A");
        when(questao.getAlternativaB()).thenReturn("Alternativa B");
        when(questao.getAlternativaC()).thenReturn("Alternativa C");
        when(questao.getAlternativaD()).thenReturn("Alternativa D");
        when(questao.getRespostaCorreta()).thenReturn("A");

        return questao;
    }

    private QuestaoRequest dadosCadastro() {
        QuestaoRequest dados = mock(QuestaoRequest.class);

        when(dados.getAvaliacaoId()).thenReturn(5L);
        when(dados.getEnunciado())
                .thenReturn("  Qual é a resposta?  ");
        when(dados.getAlternativaA())
                .thenReturn("  Alternativa A  ");
        when(dados.getAlternativaB())
                .thenReturn("  Alternativa B  ");
        when(dados.getAlternativaC())
                .thenReturn("  Alternativa C  ");
        when(dados.getAlternativaD())
                .thenReturn("  Alternativa D  ");
        when(dados.getRespostaCorreta())
                .thenReturn(" a ");

        return dados;
    }

    private QuestaoEdicaoRequest dadosEdicao() {
        QuestaoEdicaoRequest dados = mock(QuestaoEdicaoRequest.class);

        when(dados.getEnunciado())
                .thenReturn("  Enunciado atualizado  ");
        when(dados.getAlternativaA())
                .thenReturn("  Nova alternativa A  ");
        when(dados.getAlternativaB())
                .thenReturn("  Nova alternativa B  ");
        when(dados.getAlternativaC())
                .thenReturn("  Nova alternativa C  ");
        when(dados.getAlternativaD())
                .thenReturn("  Nova alternativa D  ");
        when(dados.getRespostaCorreta())
                .thenReturn(" b ");

        return dados;
    }

    private void assertErro(
            ResponseEntity<?> resposta,
            HttpStatus status,
            String mensagem) {

        assertEquals(status, resposta.getStatusCode());
        assertEquals(Map.of("erro", mensagem), resposta.getBody());
    }
}
