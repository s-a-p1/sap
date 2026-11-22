
package com.sap.service;

import com.sap.dto.QuestaoResponse;
import com.sap.model.Questao;
import com.sap.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestaoServiceTest {

    private QuestaoRepository questaoRepository;
    private QuestaoService service;

    @BeforeEach
    void configurar() {
        questaoRepository = mock(QuestaoRepository.class);
        service = new QuestaoService(questaoRepository);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremQuestoes() {
        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of());

        List<QuestaoResponse> resultado = service.listarPorAvaliacao(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(questaoRepository).findByAvaliacao_Id(1L);
    }

    @Test
    void deveConsultarQuestoesDaAvaliacaoInformada() {
        when(questaoRepository.findByAvaliacao_Id(25L))
                .thenReturn(List.of());

        service.listarPorAvaliacao(25L);

        verify(questaoRepository).findByAvaliacao_Id(25L);
        verifyNoMoreInteractions(questaoRepository);
    }

    @Test
    void deveRetornarUmaQuestao() {
        Questao questao = criarQuestao(
                10L,
                "O que é Java?",
                "Linguagem de programação",
                "Banco de dados",
                "Sistema operacional",
                "Navegador"
        );

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of(questao));

        List<QuestaoResponse> resultado = service.listarPorAvaliacao(1L);

        assertEquals(1, resultado.size());
        assertNotNull(resultado.get(0));
    }

    @Test
    void deveRetornarMultiplasQuestoes() {
        Questao primeira = criarQuestao(
                1L, "Questão 1", "A1", "B1", "C1", "D1"
        );

        Questao segunda = criarQuestao(
                2L, "Questão 2", "A2", "B2", "C2", "D2"
        );

        Questao terceira = criarQuestao(
                3L, "Questão 3", "A3", "B3", "C3", "D3"
        );

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of(primeira, segunda, terceira));

        List<QuestaoResponse> resultado = service.listarPorAvaliacao(1L);

        assertEquals(3, resultado.size());
    }

    @Test
    void devePreservarOrdemDasQuestoes() {
        Questao primeira = criarQuestao(
                30L, "Primeira questão", "A", "B", "C", "D"
        );

        Questao segunda = criarQuestao(
                10L, "Segunda questão", "A", "B", "C", "D"
        );

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of(primeira, segunda));

        List<QuestaoResponse> resultado = service.listarPorAvaliacao(1L);

        assertEquals(2, resultado.size());
        assertEquals("Primeira questão", resultado.get(0).getEnunciado());
        assertEquals("Segunda questão", resultado.get(1).getEnunciado());
    }

    @Test
    void deveMapearTodosOsDadosDaQuestao() {
        Questao questao = criarQuestao(
                50L,
                "Qual alternativa está correta?",
                "Alternativa A",
                "Alternativa B",
                "Alternativa C",
                "Alternativa D"
        );

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of(questao));

        QuestaoResponse resultado = service.listarPorAvaliacao(1L).get(0);

        assertEquals(50L, resultado.getId());
        assertEquals("Qual alternativa está correta?", resultado.getEnunciado());
        assertEquals("Alternativa A", resultado.getAlternativaA());
        assertEquals("Alternativa B", resultado.getAlternativaB());
        assertEquals("Alternativa C", resultado.getAlternativaC());
        assertEquals("Alternativa D", resultado.getAlternativaD());
    }

    @Test
    void devePreservarCamposNulos() {
        Questao questao = criarQuestao(
                1L, null, null, null, null, null
        );

        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of(questao));

        QuestaoResponse resultado = service.listarPorAvaliacao(1L).get(0);

        assertNull(resultado.getEnunciado());
        assertNull(resultado.getAlternativaA());
        assertNull(resultado.getAlternativaB());
        assertNull(resultado.getAlternativaC());
        assertNull(resultado.getAlternativaD());
    }

    @Test
    void deveConsultarRepositorioUmaUnicaVez() {
        when(questaoRepository.findByAvaliacao_Id(1L))
                .thenReturn(List.of());

        service.listarPorAvaliacao(1L);

        verify(questaoRepository, times(1)).findByAvaliacao_Id(1L);
        verifyNoMoreInteractions(questaoRepository);
    }

    @Test
    void deveEncaminharIdNuloAoRepositorio() {
        when(questaoRepository.findByAvaliacao_Id(null))
                .thenReturn(List.of());

        List<QuestaoResponse> resultado = service.listarPorAvaliacao(null);

        assertTrue(resultado.isEmpty());
        verify(questaoRepository).findByAvaliacao_Id(null);
    }

    private Questao criarQuestao(
            Long id,
            String enunciado,
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD
    ) {
        Questao questao = mock(Questao.class);

        when(questao.getId()).thenReturn(id);
        when(questao.getEnunciado()).thenReturn(enunciado);
        when(questao.getAlternativaA()).thenReturn(alternativaA);
        when(questao.getAlternativaB()).thenReturn(alternativaB);
        when(questao.getAlternativaC()).thenReturn(alternativaC);
        when(questao.getAlternativaD()).thenReturn(alternativaD);

        return questao;
    }
}
