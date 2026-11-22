
package com.sap.controller;

import com.sap.model.Disciplina;
import com.sap.service.DisciplinaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisciplinaControllerTest {

    private DisciplinaService disciplinaService;
    private DisciplinaController controller;

    @BeforeEach
    void configurar() {
        disciplinaService = mock(DisciplinaService.class);
        controller = new DisciplinaController(disciplinaService);
    }

    @Test
    void deveListarTodasAsDisciplinas() {
        Disciplina primeira = mock(Disciplina.class);
        Disciplina segunda = mock(Disciplina.class);

        List<Disciplina> disciplinas = List.of(primeira, segunda);

        when(disciplinaService.listarTodas())
                .thenReturn(disciplinas);

        List<Disciplina> resposta = controller.listarTodas();

        assertEquals(2, resposta.size());
        assertSame(primeira, resposta.get(0));
        assertSame(segunda, resposta.get(1));

        verify(disciplinaService).listarTodas();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemDisciplinas() {
        when(disciplinaService.listarTodas())
                .thenReturn(List.of());

        List<Disciplina> resposta = controller.listarTodas();

        assertNotNull(resposta);
        assertTrue(resposta.isEmpty());
    }

    @Test
    void deveCadastrarDisciplinaComSucesso() {
        Disciplina disciplina = mock(Disciplina.class);
        Disciplina salva = mock(Disciplina.class);

        when(disciplinaService.cadastrar(disciplina))
                .thenReturn(salva);

        ResponseEntity<Disciplina> resposta =
                controller.cadastrar(disciplina);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertSame(salva, resposta.getBody());

        verify(disciplinaService).cadastrar(disciplina);
    }

    @Test
    void deveEncaminharDisciplinaCorretaAoService() {
        Disciplina disciplina = mock(Disciplina.class);

        controller.cadastrar(disciplina);

        verify(disciplinaService, times(1)).cadastrar(disciplina);
    }

    @Test
    void devePropagarErroDeCadastroDoService() {
        Disciplina disciplina = mock(Disciplina.class);

        when(disciplinaService.cadastrar(disciplina))
                .thenThrow(new IllegalArgumentException(
                        "Disciplina inválida."
                ));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> controller.cadastrar(disciplina)
        );

        assertEquals("Disciplina inválida.", erro.getMessage());
    }

    @Test
    void deveExcluirDisciplinaComSucesso() {
        ResponseEntity<Void> resposta = controller.excluir(10L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        assertNull(resposta.getBody());

        verify(disciplinaService).excluir(10L);
    }

    @Test
    void deveEncaminharIdCorretoNaExclusao() {
        controller.excluir(25L);

        verify(disciplinaService, times(1)).excluir(25L);
    }

    @Test
    void devePropagarErroDeExclusaoDoService() {
        doThrow(new IllegalStateException(
                "Disciplina possui vínculos."
        )).when(disciplinaService).excluir(10L);

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> controller.excluir(10L)
        );

        assertEquals("Disciplina possui vínculos.", erro.getMessage());
    }

    @Test
    void deveTratarDisciplinaNaoEncontrada() {
        IllegalArgumentException erro =
                new IllegalArgumentException(
                        "Disciplina não encontrada."
                );

        ResponseEntity<Map<String, String>> resposta =
                controller.tratarDisciplinaNaoEncontrada(erro);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertEquals(
                Map.of("erro", "Disciplina não encontrada."),
                resposta.getBody()
        );
    }

    @Test
    void deveTratarDisciplinaComVinculos() {
        IllegalStateException erro =
                new IllegalStateException(
                        "Disciplina possui vínculos."
                );

        ResponseEntity<Map<String, String>> resposta =
                controller.tratarDisciplinaComVinculos(erro);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals(
                Map.of("erro", "Disciplina possui vínculos."),
                resposta.getBody()
        );
    }
}
