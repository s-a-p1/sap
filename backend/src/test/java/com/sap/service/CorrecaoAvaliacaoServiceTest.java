
package com.sap.service;

import com.sap.dto.FinalizarAvaliacaoRequest;
import com.sap.dto.RespostaQuestaoRequest;
import com.sap.dto.ResultadoAvaliacaoResponse;
import com.sap.model.AnaliseDesempenho;
import com.sap.model.Assunto;
import com.sap.model.Avaliacao;
import com.sap.model.ConfiguracaoAcademica;
import com.sap.model.Curso;
import com.sap.model.DesempenhoAssunto;
import com.sap.model.Disciplina;
import com.sap.model.Questao;
import com.sap.model.Resultado;
import com.sap.model.Usuario;
import com.sap.repository.AvaliacaoRepository;
import com.sap.repository.ConfiguracaoAcademicaRepository;
import com.sap.repository.DesempenhoAssuntoRepository;
import com.sap.repository.MatriculaDisciplinaRepository;
import com.sap.repository.QuestaoRepository;
import com.sap.repository.ResultadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CorrecaoAvaliacaoServiceTest {

    private AvaliacaoRepository avaliacaoRepository;
    private QuestaoRepository questaoRepository;
    private ResultadoRepository resultadoRepository;
    private DesempenhoAssuntoRepository desempenhoAssuntoRepository;
    private ConfiguracaoAcademicaRepository configuracaoRepository;
    private MatriculaDisciplinaRepository matriculaRepository;
    private AnaliseDesempenhoService analiseService;
    private AuditoriaService auditoriaService;
    private CorrecaoAvaliacaoService service;

    @BeforeEach
    void configurar() {
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        questaoRepository = mock(QuestaoRepository.class);
        resultadoRepository = mock(ResultadoRepository.class);
        desempenhoAssuntoRepository = mock(DesempenhoAssuntoRepository.class);
        configuracaoRepository = mock(ConfiguracaoAcademicaRepository.class);
        matriculaRepository = mock(MatriculaDisciplinaRepository.class);
        analiseService = mock(AnaliseDesempenhoService.class);
        auditoriaService = mock(AuditoriaService.class);

        service = new CorrecaoAvaliacaoService(
                avaliacaoRepository,
                questaoRepository,
                resultadoRepository,
                desempenhoAssuntoRepository,
                configuracaoRepository,
                matriculaRepository,
                analiseService,
                auditoriaService
        );
    }

    @Test
    void deveRejeitarRequestNulo() {
        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(null, usuario()));
    }

    @Test
    void deveRejeitarAvaliacaoSemId() {
        FinalizarAvaliacaoRequest request =
                mock(FinalizarAvaliacaoRequest.class);

        when(request.getAvaliacaoId()).thenReturn(null);

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request, usuario()));
    }

    @Test
    void deveRejeitarUsuarioNulo() {
        verificarErro(HttpStatus.UNAUTHORIZED,
                () -> service.corrigir(request(1L), null));
    }

    @Test
    void deveRejeitarUsuarioSemId() {
        verificarErro(HttpStatus.UNAUTHORIZED,
                () -> service.corrigir(request(1L), new Usuario()));
    }

    @Test
    void deveRejeitarAvaliacaoInexistente() {
        when(avaliacaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        verificarErro(HttpStatus.NOT_FOUND,
                () -> service.corrigir(request(1L), usuario()));
    }

    @Test
    void deveRejeitarAvaliacaoSemAssunto() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(1L);

        when(avaliacaoRepository.findById(1L))
                .thenReturn(Optional.of(avaliacao));

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request(1L), usuario()));
    }

    @Test
    void deveRejeitarAvaliacaoSemDisciplina() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(1L);
        avaliacao.setAssunto(new Assunto());

        when(avaliacaoRepository.findById(1L))
                .thenReturn(Optional.of(avaliacao));

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request(1L), usuario()));
    }

    @Test
    void deveRejeitarEstudanteSemConfiguracao() {
        prepararAvaliacao();

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.empty());

        verificarErro(HttpStatus.FORBIDDEN,
                () -> service.corrigir(request(1L), usuario()));
    }

    @Test
    void deveRejeitarEstudanteNaoMatriculado() {
        prepararMatricula(false);

        verificarErro(HttpStatus.FORBIDDEN,
                () -> service.corrigir(request(1L), usuario()));
    }

    @Test
    void deveRejeitarAvaliacaoSemQuestoes() {
        prepararMatricula(true);

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of());

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request(1L), usuario()));
    }

    @Test
    void deveRejeitarRespostaNula() {
        prepararQuestoes();

        FinalizarAvaliacaoRequest request = request(1L);

        List<RespostaQuestaoRequest> respostas =
                Arrays.asList((RespostaQuestaoRequest) null);

        when(request.getRespostas()).thenReturn(respostas);

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request, usuario()));
    }

    @Test
    void deveRejeitarRespostaSemQuestao() {
        prepararQuestoes();

        FinalizarAvaliacaoRequest request = request(1L);
        RespostaQuestaoRequest resposta = mock(RespostaQuestaoRequest.class);

        when(request.getRespostas()).thenReturn(List.of(resposta));

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request, usuario()));
    }

    @Test
    void deveRejeitarQuestaoDeOutraAvaliacao() {
        prepararQuestoes();

        FinalizarAvaliacaoRequest request = request(1L);
        RespostaQuestaoRequest respostaInvalida = resposta(99L, "A");

        when(request.getRespostas())
                .thenReturn(List.of(respostaInvalida));

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request, usuario()));
    }

    @Test
    void deveRejeitarRespostaDuplicada() {
        prepararQuestoes();

        FinalizarAvaliacaoRequest request = request(1L);
        RespostaQuestaoRequest primeira = resposta(10L, "A");
        RespostaQuestaoRequest segunda = resposta(10L, "B");

        when(request.getRespostas())
                .thenReturn(List.of(primeira, segunda));

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request, usuario()));
    }

    @Test
    void deveRejeitarAlternativaInvalida() {
        prepararQuestoes();

        FinalizarAvaliacaoRequest request = request(1L);
        RespostaQuestaoRequest respostaInvalida = resposta(10L, "E");

        when(request.getRespostas())
                .thenReturn(List.of(respostaInvalida));

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request, usuario()));
    }

    @Test
    void deveRejeitarQuestaoSemAssunto() {
        prepararMatricula(true);

        Questao questao = mock(Questao.class);
        when(questao.getId()).thenReturn(10L);

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of(questao));

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request(1L), usuario()));
    }

    @Test
    void deveRejeitarQuestaoDeOutraDisciplina() {
        prepararMatricula(true);

        Disciplina outraDisciplina = new Disciplina();
        outraDisciplina.setId(99L);

        Assunto outroAssunto = new Assunto();
        outroAssunto.setId(8L);
        outroAssunto.setDisciplina(outraDisciplina);

        Questao questao = mock(Questao.class);
        when(questao.getId()).thenReturn(10L);
        when(questao.getAssunto()).thenReturn(outroAssunto);

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of(questao));

        verificarErro(HttpStatus.BAD_REQUEST,
                () -> service.corrigir(request(1L), usuario()));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0.00",
            "49, 49.00",
            "50, 50.00",
            "69, 69.00",
            "70, 70.00",
            "100, 100.00"
    })
    void deveCalcularPercentualCorretamente(
            int quantidadeAcertos,
            String percentualEsperado) {

        prepararMatricula(true);
        prepararPersistencia();

        List<Questao> questoes = new ArrayList<>();
        List<RespostaQuestaoRequest> respostas = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            Long id = (long) i;
            questoes.add(questaoValida(id));

            String alternativa = i <= quantidadeAcertos ? "A" : "B";
            respostas.add(resposta(id, alternativa));
        }

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(questoes);

        FinalizarAvaliacaoRequest request = request(1L);
        when(request.getRespostas()).thenReturn(respostas);

        ResultadoAvaliacaoResponse response =
                service.corrigir(request, usuario());

        assertNotNull(response);

        org.mockito.ArgumentCaptor<Resultado> captor =
                org.mockito.ArgumentCaptor.forClass(Resultado.class);

        verify(resultadoRepository).save(captor.capture());

        Resultado resultado = captor.getValue();

        assertEquals(100, resultado.getTotalQuestoes());
        assertEquals(quantidadeAcertos, resultado.getAcertos());
        assertEquals(100 - quantidadeAcertos, resultado.getErros());

        assertEquals(
                0,
                new BigDecimal(percentualEsperado)
                        .compareTo(resultado.getPercentual())
        );

        verify(desempenhoAssuntoRepository)
                .save(any(DesempenhoAssunto.class));

        verify(analiseService).analisarResultado(resultado);

        verify(auditoriaService).registrar(
                eq(1L),
                eq("estudante@teste.com"),
                eq("FINALIZOU_AVALIACAO"),
                contains("finalizada")
        );
    }

    @Test
    void deveAceitarAlternativaMinusculaComEspacos() {
        prepararMatricula(true);
        prepararPersistencia();

        Questao questao = questaoValida(10L);

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of(questao));

        FinalizarAvaliacaoRequest request = request(1L);
        RespostaQuestaoRequest resposta = resposta(10L, " a ");

        when(request.getRespostas())
                .thenReturn(List.of(resposta));

        service.corrigir(request, usuario());

        org.mockito.ArgumentCaptor<Resultado> captor =
                org.mockito.ArgumentCaptor.forClass(Resultado.class);

        verify(resultadoRepository).save(captor.capture());

        assertEquals(1, captor.getValue().getAcertos());
        assertEquals(0, captor.getValue().getErros());
    }

    @Test
void deveConsiderarRespostaEmBrancoComoErro() {
    prepararMatricula(true);
    prepararPersistencia();

    Questao questao = questaoValida(10L);

    when(questaoRepository.findByAvaliacao_Id(1L))
            .thenReturn(List.of(questao));

    FinalizarAvaliacaoRequest request = request(1L);
    RespostaQuestaoRequest resposta = resposta(10L, "   ");

    when(request.getRespostas())
            .thenReturn(List.of(resposta));

    service.corrigir(request, usuario());

    org.mockito.ArgumentCaptor<Resultado> captor =
            org.mockito.ArgumentCaptor.forClass(Resultado.class);

    verify(resultadoRepository).save(captor.capture());

    assertEquals(0, captor.getValue().getAcertos());
    assertEquals(1, captor.getValue().getErros());
}

    @Test
void devePermitirAvaliacaoSemRespostas() {
    prepararMatricula(true);
    prepararPersistencia();

    Questao questao = questaoValida(10L);

    when(questaoRepository.findByAvaliacao_Id(1L))
            .thenReturn(List.of(questao));

    ResultadoAvaliacaoResponse response =
            service.corrigir(request(1L), usuario());

    assertNotNull(response);

    org.mockito.ArgumentCaptor<Resultado> captor =
            org.mockito.ArgumentCaptor.forClass(Resultado.class);

    verify(resultadoRepository).save(captor.capture());

    assertEquals(0, captor.getValue().getAcertos());
    assertEquals(1, captor.getValue().getErros());
}

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("estudante@teste.com");
        return usuario;
    }

    private FinalizarAvaliacaoRequest request(Long id) {
        FinalizarAvaliacaoRequest request =
                mock(FinalizarAvaliacaoRequest.class);

        doReturn(id).when(request).getAvaliacaoId();

        return request;
    }

    private RespostaQuestaoRequest resposta(Long id, String alternativa) {
        RespostaQuestaoRequest resposta =
                mock(RespostaQuestaoRequest.class);

        doReturn(id).when(resposta).getQuestaoId();
        doReturn(alternativa).when(resposta).getResposta();

        return resposta;
    }

    private void prepararAvaliacao() {
        Disciplina disciplina = new Disciplina();
        disciplina.setId(5L);

        Assunto assunto = new Assunto();
        assunto.setId(7L);
        assunto.setNome("Assunto de teste");
        assunto.setDisciplina(disciplina);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(1L);
        avaliacao.setAssunto(assunto);

        when(avaliacaoRepository.findById(1L))
                .thenReturn(Optional.of(avaliacao));
    }

    private void prepararMatricula(boolean matriculado) {
        prepararAvaliacao();

        Curso curso = new Curso();
        curso.setId(2L);

        ConfiguracaoAcademica configuracao =
                new ConfiguracaoAcademica();

        configuracao.setCurso(curso);
        configuracao.setSemestre(1);

        when(configuracaoRepository.findByUsuarioId(1L))
                .thenReturn(Optional.of(configuracao));

        when(matriculaRepository
                .existsByConfiguracaoAcademica_Usuario_IdAndOfertaDisciplina_Disciplina_IdAndOfertaDisciplina_Curso_IdAndOfertaDisciplina_Semestre(
                        1L, 5L, 2L, 1
                ))
                .thenReturn(matriculado);
    }

    private void prepararQuestoes() {
        prepararMatricula(true);

        Questao questao = new Questao();
        questao.setId(10L);

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of(questao));
    }

    private Questao questaoValida(Long id) {
        Disciplina disciplina = new Disciplina();
        disciplina.setId(5L);

        Assunto assunto = new Assunto();
        assunto.setId(7L);
        assunto.setNome("Assunto de teste");
        assunto.setDisciplina(disciplina);

        Questao questao = mock(Questao.class);

        when(questao.getId()).thenReturn(id);
        when(questao.getAssunto()).thenReturn(assunto);
        when(questao.getRespostaCorreta()).thenReturn("A");

        return questao;
    }

    private void prepararPersistencia() {
    when(resultadoRepository.save(any(Resultado.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

    AnaliseDesempenho analise = mock(AnaliseDesempenho.class);

    when(analiseService.analisarResultado(any(Resultado.class)))
            .thenReturn(analise);
}

    private void verificarErro(
            HttpStatus status,
            org.junit.jupiter.api.function.Executable acao) {

        ResponseStatusException erro =
                assertThrows(ResponseStatusException.class, acao);

        assertEquals(status, erro.getStatusCode());

        verifyNoInteractions(
                resultadoRepository,
                desempenhoAssuntoRepository,
                analiseService,
                auditoriaService
        );
    }
}
