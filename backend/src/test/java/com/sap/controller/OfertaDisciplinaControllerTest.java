
package com.sap.controller;

import com.sap.model.Disciplina;
import com.sap.model.OfertaDisciplina;
import com.sap.repository.OfertaDisciplinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OfertaDisciplinaControllerTest {

    private OfertaDisciplinaRepository ofertaDisciplinaRepository;
    private OfertaDisciplinaController controller;

    @BeforeEach
    void configurar() {
        ofertaDisciplinaRepository = mock(OfertaDisciplinaRepository.class);

        controller = new OfertaDisciplinaController(
                ofertaDisciplinaRepository
        );
    }

    @Test
    void deveListarOfertasDeUmCursoESemestre() {
        OfertaDisciplina primeira = oferta(
                10L, 1L, "Programação", 1
        );

        OfertaDisciplina segunda = oferta(
                11L, 2L, "Banco de Dados", 1
        );

        when(ofertaDisciplinaRepository.findByCursoIdAndSemestre(5L, 1))
                .thenReturn(List.of(primeira, segunda));

        List<Map<String, Object>> resposta =
                controller.listar(5L, 1);

        assertEquals(2, resposta.size());

        assertEquals(10L, resposta.get(0).get("id"));
        assertEquals(1L, resposta.get(0).get("disciplinaId"));
        assertEquals("Programação", resposta.get(0).get("disciplinaNome"));
        assertEquals(1, resposta.get(0).get("semestre"));

        assertEquals(11L, resposta.get(1).get("id"));
        assertEquals(2L, resposta.get(1).get("disciplinaId"));
        assertEquals("Banco de Dados", resposta.get(1).get("disciplinaNome"));
        assertEquals(1, resposta.get(1).get("semestre"));

        verify(ofertaDisciplinaRepository)
                .findByCursoIdAndSemestre(5L, 1);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemOfertas() {
        when(ofertaDisciplinaRepository.findByCursoIdAndSemestre(5L, 8))
                .thenReturn(List.of());

        List<Map<String, Object>> resposta =
                controller.listar(5L, 8);

        assertNotNull(resposta);
        assertTrue(resposta.isEmpty());
    }

    @Test
    void deveConsultarCursoCorreto() {
        controller.listar(15L, 2);

        verify(ofertaDisciplinaRepository)
                .findByCursoIdAndSemestre(15L, 2);
    }

    @Test
    void deveConsultarSemestreCorreto() {
        controller.listar(5L, 6);

        verify(ofertaDisciplinaRepository)
                .findByCursoIdAndSemestre(5L, 6);
    }

    @Test
    void deveRetornarOfertaDeOutroSemestre() {
        OfertaDisciplina oferta = oferta(
                20L, 3L, "Engenharia de Software", 4
        );

        when(ofertaDisciplinaRepository.findByCursoIdAndSemestre(5L, 4))
                .thenReturn(List.of(oferta));

        List<Map<String, Object>> resposta =
                controller.listar(5L, 4);

        assertEquals(1, resposta.size());
        assertEquals(4, resposta.get(0).get("semestre"));
        assertEquals("Engenharia de Software",
                resposta.get(0).get("disciplinaNome"));
    }

    @Test
    void devePreservarIdentificadoresDasOfertas() {
        OfertaDisciplina oferta = oferta(
                99L, 45L, "Estrutura de Dados", 3
        );

        when(ofertaDisciplinaRepository.findByCursoIdAndSemestre(7L, 3))
                .thenReturn(List.of(oferta));

        List<Map<String, Object>> resposta =
                controller.listar(7L, 3);

        assertEquals(99L, resposta.get(0).get("id"));
        assertEquals(45L, resposta.get(0).get("disciplinaId"));
    }

    @Test
    void deveRetornarUmaOfertaQuandoExisteApenasUma() {
        OfertaDisciplina oferta = oferta(
                30L, 8L, "Matemática", 2
        );

        when(ofertaDisciplinaRepository.findByCursoIdAndSemestre(1L, 2))
                .thenReturn(List.of(oferta));

        List<Map<String, Object>> resposta =
                controller.listar(1L, 2);

        assertEquals(1, resposta.size());
        assertEquals("Matemática", resposta.get(0).get("disciplinaNome"));
    }

    @Test
    void devePropagarErroDoRepositorio() {
        when(ofertaDisciplinaRepository.findByCursoIdAndSemestre(5L, 1))
                .thenThrow(new IllegalStateException(
                        "Erro ao consultar ofertas."
                ));

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> controller.listar(5L, 1)
        );

        assertEquals("Erro ao consultar ofertas.", erro.getMessage());
    }

    private OfertaDisciplina oferta(
            Long id,
            Long disciplinaId,
            String disciplinaNome,
            Integer semestre) {

        OfertaDisciplina oferta = mock(OfertaDisciplina.class);
        Disciplina disciplina = mock(Disciplina.class);

        when(oferta.getId()).thenReturn(id);
        when(oferta.getSemestre()).thenReturn(semestre);
        when(oferta.getDisciplina()).thenReturn(disciplina);

        when(disciplina.getId()).thenReturn(disciplinaId);
        when(disciplina.getNome()).thenReturn(disciplinaNome);

        return oferta;
    }
}
